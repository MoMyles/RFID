package com.pugongying.uhf;

import android.util.Log;

import com.uhf.uhf.Common.InventoryBuffer;

public class CodeUtil {

    private static String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }

        return sb.toString();
    }


    public static String getDecodeStr(InventoryBuffer.InventoryTagMap map){
        String epc = "";
        if (map.strTID != null && map.strTID.contains("扫描获得")) {
            epc = map.strEPC.replace(" ", "");
        } else {
            epc = map.strEPC.replace(" ", "");
            epc = CodeUtil.convertHexToString(epc);
        }
        return epc == null ? "" : epc.trim();
    }

}
