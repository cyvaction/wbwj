package util;

import filter.BaseFilter;
import jxl.write.WriteException;
import locator.Locator;
import model.ExcelModel;
import model.SSDModel;
import org.xml.sax.SAXException;
import parser.SSDParser;
import parser.SSDParserByDate;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by raychen on 2016/12/11.
 */
public class TestUtil {

    //测试分析器
    public static void testParser(String filesPath) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        File file=new File(XMLUtil.readPath+filesPath);
        String filenames[];
        filenames = file.list();
        Locator locator = new Locator();
        SSDParser parser = new SSDParserByDate();
        BaseFilter filter = new BaseFilter();
        int count = 0;
        //读取子文件
        for (String filename: filenames) {
            if (filename.charAt(0) == '.') continue;
            //获得事实段
            ArrayList<SSDModel> res = locator.getSSDs(filesPath, XMLUtil.readPath+filesPath+"/"+filename);
            if (res.size() == 0) continue;
            //分解事实段
            ArrayList<SSDModel> parseRes = parser.parseSSD(res);
            //过滤事实
            ArrayList<SSDModel> filteredRes = filter.filter(parseRes);
            //去除null
            if (filteredRes.size() == 0) continue;
            count ++;
            //生成xml
            XMLUtil.buildNewSSD(filteredRes, filesPath+"/"+filename);
        }
        System.out.println("parse: "+count+"/"+filenames.length);
    }

    public static void testExcel() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException, WriteException {
        File newFile = new File(ExcelUtil.filePath+"事实段表格.xls");
        if (!newFile.exists()) newFile.createNewFile();
        OutputStream os = new FileOutputStream(newFile);
        //读取xml
        File file_out=new File(XMLUtil.readPath);
        String fileout_names[] = file_out.list();
        ArrayList<ExcelModel> models = new ArrayList<ExcelModel>();
        for (String fileout_name: fileout_names) {
            if (fileout_name.charAt(0) == '.') continue;
            //内层
            File file = new File(XMLUtil.readPath+fileout_name);
            String filenames[] = file.list();
            for (String filename: filenames) {
                if (filename.charAt(0) == '.') continue;
                String x = fileout_name.contains("一审")?"CMSSD":"QSSLD";
                ArrayList<String> results = XMLUtil.getNodes(XMLUtil.readPath+fileout_name+"/"+filename, "//"+x+"/@value");
                //存在事实段，插入表格
                if (results.size() > 0){
                    for (String str: results) {
                        ExcelModel model = new ExcelModel(filename, str, x.equals("CMSSD")?"查明事实段":"前审审理段", fileout_name);
                        models.add(model);
                    }
                }
            }
        }
        ExcelUtil.buildExcel(os, models);
    }
}
