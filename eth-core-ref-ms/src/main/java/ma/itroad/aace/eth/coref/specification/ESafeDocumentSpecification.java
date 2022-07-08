package ma.itroad.aace.eth.coref.specification;

import ma.itroad.aace.eth.coref.model.entity.ESafeDocument;
import ma.itroad.aace.eth.coref.model.entity.EconomicOperator;
import ma.itroad.aace.eth.coref.model.view.EsafeDocumentFilterPayload;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

// TODO implement jpa.domain.Specification instead of static implementations
public class ESafeDocumentSpecification {

    private ESafeDocumentSpecification() {}

    public static Specification<ESafeDocument> hasOwner(Long ownerId, String createdBy) {
        return (eSafeDocument, cq, cb) -> {
            Join join = eSafeDocument.join("owner");
            return cb.or(cb.equal(join.get("id"), ownerId), cb.equal(eSafeDocument.get("createdBy"), createdBy));
        };
    }

    public static Specification<ESafeDocument> hasOwner(String createdBy) {
        return (eSafeDocument, cq, cb) -> cb.equal(eSafeDocument.get("createdBy"), createdBy);
    }

    public static Specification<ESafeDocument> shardByCurrentUser(Long ownerId, String createdBy) {
        return (eSafeDocument, cq, cb) -> {
            Join join = eSafeDocument.join("shares").join("document");
            Join ownerJoin = eSafeDocument.join("owner");
            cq.distinct(true);
            return cb.and(cb.or(cb.equal(eSafeDocument.get("createdBy"),
                    createdBy), cb.equal(ownerJoin.get("id"), ownerId)),
                    cb.isNotNull(join.get("id")));
        };
    }

    public static Specification<ESafeDocument> visibleWithCurrentUser(Long currentUserId) {
        return (eSafeDocument, cq, cb) -> {
            Join join = eSafeDocument.join("permissions");
            Join userJoin = join.join("userAccount");
            cq.distinct(true);
            return cb.and(cb.equal(join.get("visible"), true), cb.equal(userJoin.get("id"), currentUserId));
        };
    }

    public static Specification<ESafeDocument> shardByCurrentUser(String createdBy) {
        return (eSafeDocument, cq, cb) -> {
            Join join = eSafeDocument.join("shares").join("document");
            cq.distinct(true);
            return cb.and(cb.equal(eSafeDocument.get("createdBy"),
                    createdBy),
                    cb.isNotNull(join.get("id")));
        };
    }

    public static Specification<ESafeDocument> shardWithCurrentUser(Long ownerId) {
        return (eSafeDocument, cq, cb) -> {
            Join join = eSafeDocument.join("shares").join("userAccount");
            cq.distinct(true);
            return cb.equal(join.get("id"), ownerId);
        };
    }

    public static Specification<ESafeDocument> byTitle(String title) {
        return (eSafeDocument, cq, cb) ->   cb.like(eSafeDocument.get("title"), "%"+title+"%");
    }
    public static Specification<ESafeDocument> byReference(String ref) {
        return (eSafeDocument, cq, cb) ->   cb.equal(eSafeDocument.get("reference"), "%"+ref+"%");
    }
    public static Specification<ESafeDocument> byEconomicOperator(Long id) {
        return (root, criteriaQuery, cb) -> cb.equal(root.get("economicOperator"), id);
    }
    public static Specification<ESafeDocument> byEconomicOperatorCode(String code) {

        return (root, q, cb) ->{

            Subquery<EconomicOperator> subquery = q.subquery(EconomicOperator.class);
            Root<EconomicOperator> ecoOpe = subquery.from(EconomicOperator.class);
            CriteriaQuery<ESafeDocument> criteriaQuery =
                    cb.createQuery(ESafeDocument.class);
            subquery.select(ecoOpe)
                    .distinct(true)
                    .where(cb.like(ecoOpe.get("code"), "%" + code + "%"));

           return criteriaQuery.select(root)
                    .where(cb.in(root.get("economicOperator")).value(subquery)).getRestriction();

        } ;
    }

    public static Specification<ESafeDocument> bydate(String dateString) {
        return (root, criteriaQuery, cb) -> {
            Path<LocalDateTime> ldt = root.<LocalDateTime>get( "createdOn");
            Expression<Integer> dbYear = cb.function("year", Integer.class, ldt);
            Expression<Integer> dbMonth = cb.function("month", Integer.class, ldt);
            Expression<Integer> dbDay = cb.function("day", Integer.class, ldt);
            Calendar calendar = stringTodate(dateString);

            return cb.and(cb.equal(dbYear, calendar.get(Calendar.YEAR))
                    , cb.equal(dbMonth, calendar.get(Calendar.MONTH) + 1)
                    , cb.equal(dbDay, calendar.get(Calendar.DAY_OF_MONTH)));
        };
    }
    public static Calendar stringTodate(String dateString){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        Calendar calendar = new GregorianCalendar();
        try {
            if(dateString==null){
                return calendar;
            }
            date= format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

         calendar.setTime(date);
        return calendar;
    }
    public static Specification<ESafeDocument> filter(EsafeDocumentFilterPayload esafeDocumentFilterPayload) {
        return (root, criteriaQuery, cb) -> {
            Predicate predicate1=byReference(esafeDocumentFilterPayload.getReference()).toPredicate(root,criteriaQuery,cb);
            Predicate predicate2=byTitle(esafeDocumentFilterPayload.getTitle()).toPredicate(root,criteriaQuery,cb);
            Predicate predicate5=bydate(esafeDocumentFilterPayload.getCreationDate()).toPredicate(root,criteriaQuery,cb);
            Predicate predicate4=cb.equal(root.get("auteur"),esafeDocumentFilterPayload.getAuteur());

            Predicate predicate3=cb.or(predicate1,predicate2,predicate4,predicate5);


           Predicate filterPredicate=cb.or(predicate3,byEconomicOperatorCode(esafeDocumentFilterPayload.getEcoCode())
            .toPredicate(root,criteriaQuery,cb)
           );
              //  return cb.and(prepredicate,cb.equal(join.get("id"), createdby));
           // return cb.and(prepredicate,hasOwner(createdby,"").toPredicate(root,criteriaQuery,cb));
            return filterPredicate;
        };
    }

    public static Specification<ESafeDocument> ownedByCurrentUserFilter(EsafeDocumentFilterPayload esafeDocumentFilterPayload,Long createdby){

        return(r,cq,cb)-> cb.and(filter(esafeDocumentFilterPayload).toPredicate(r,cq,cb)
                ,hasOwner(createdby,"").toPredicate(r,cq,cb));

    }
    public static Specification<ESafeDocument> visibleWithCurrentUserFilter(EsafeDocumentFilterPayload esafeDocumentFilterPayload,Long createdby){
        return(r,cq,cb)-> cb.and(filter(esafeDocumentFilterPayload).toPredicate(r,cq,cb),visibleWithCurrentUser(createdby).toPredicate(r,cq,cb));
    }
    public static Specification<ESafeDocument> sharedWithCurrentUserFilter(EsafeDocumentFilterPayload esafeDocumentFilterPayload,Long createdby){
        return(r,cq,cb)-> cb.and(filter(esafeDocumentFilterPayload).toPredicate(r,cq,cb),shardWithCurrentUser(createdby).toPredicate(r,cq,cb));
    }
    public static Specification<ESafeDocument> sharedByCurrentUserFilter(EsafeDocumentFilterPayload esafeDocumentFilterPayload,Long id,String createdby){
        return(r,cq,cb)-> cb.and(filter(esafeDocumentFilterPayload).toPredicate(r,cq,cb),shardByCurrentUser(id,createdby).toPredicate(r,cq,cb));
    }

}
