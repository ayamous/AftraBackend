package ma.itroad.aace.eth.coref.service.util;

import org.apache.poi.ss.usermodel.Cell;

public class Util {
    public static String cellValue( Cell cell) {
        switch (cell.getCellType()) {
            case 0:
                return Integer.valueOf((int) cell.getNumericCellValue()).toString();
            case 1:
                return cell.getStringCellValue() ;
            default:return null ;
        }
    }
}
