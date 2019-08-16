package cn.oi.klittle.era.mian;

import org.jetbrains.annotations.NotNull;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import cn.oi.klittle.era.sdk.youdao.KYouDaoHttp;
import cn.oi.klittle.era.sdk.youdao.entity.KYouDaoLanguageEntity;

/**
 * fixme 有道翻译
 */
public class KYoudao {

    public static void main(String[] args) {
        System.out.println("开始:\t");

        String sourcePath = "D:\\java\\strings\\src\\strings.xml";//源文件
        String from = "zh-CHS";//从什么语言
        String to = "en";//翻译到什么语言
        //String outPath = "D:\\java\\strings\\out\\values-" + to;//输入文件路径;不要以\\结尾。
        String outPath = "D:\\java\\strings\\out\\values-";
        String outFileName = "strings.xml";
        //translateAndroidXmlString(sourcePath, outPath + to, outFileName, from, to);

        ArrayList<KYouDaoLanguageEntity> entities = new ArrayList<>();//语言数组

        KYouDaoLanguageEntity language_en = new KYouDaoLanguageEntity();//英文
        language_en.setSourcePath(sourcePath);
        language_en.setFrom(from);
        language_en.setTo(to);
        language_en.setOutFileName(outFileName);
        language_en.setOutPath(outPath + to);
        entities.add(language_en);

        KYouDaoLanguageEntity language_ja = language_en.copy();//日文
        language_ja.setTo("ja");
        language_ja.setOutPath(outPath + language_ja.getTo());
        entities.add(language_ja);

//        KYouDaoLanguageEntity language_ko = language_en.copy();//韩文
//        language_ko.setTo("ko");
//        language_ko.setOutPath(outPath + language_ko.getTo());
//        entities.add(language_ko);
//
//        KYouDaoLanguageEntity language_fr = language_en.copy();//法文
//        language_fr.setTo("fr");
//        language_fr.setOutPath(outPath + language_fr.getTo());
//        entities.add(language_fr);
//
//        KYouDaoLanguageEntity language_de = language_en.copy();//德文
//        language_de.setTo("de");
//        language_de.setOutPath(outPath + language_de.getTo());
//        entities.add(language_de);
//
//        KYouDaoLanguageEntity language_ru = language_en.copy();//俄文
//        language_ru.setTo("ru");
//        language_ru.setOutPath(outPath + language_ru.getTo());
//        entities.add(language_ru);

        translateAndroidXmlString(entities);

        System.out.println("结束");
    }

    //translateAndroidXmlString("D:\\github\\AndroidStringTranslate\\src\\res\\strings.xml","D:\\github\\AndroidStringTranslate\\out\\result.xml");

    public static void translateAndroidXmlString(String sourcePath, String outPath, String outFileName) {
        translateAndroidXmlString(sourcePath, outPath, outFileName, "zh-CHS", "en");//默认从中文翻译成英文。
    }

    public static void translateAndroidXmlString(ArrayList<KYouDaoLanguageEntity> entities) {
        if (entities == null && entities.size() > 0) {
            return;
        }
        for (int i = 0; i < entities.size(); i++) {
            translateAndroidXmlString(entities.get(i));//循环翻译语言数组；亲测有效。
        }
    }

    public static void translateAndroidXmlString(KYouDaoLanguageEntity entity) {
        if (entity == null) {
            return;
        }
        translateAndroidXmlString(entity.getSourcePath(), entity.getOutPath(), entity.getOutFileName(), entity.getFrom(), entity.getTo());//默认从中文翻译成英文。
    }

    /**
     * 翻译的总入口  需要传递两个路径
     *
     * @param sourcePath  源文件的绝对路径（包括文件名及后缀）
     * @param outPath     输出文件的绝对路径（不包括文件名）;不要以\\结尾。
     * @param outFileName 输出文件名称（文件名，包含后缀）
     * @param from        从什么语言
     * @param to          翻译到什么语言；fixme 这个语言编码和android国际语言编码基本一致。
     */
    public static void translateAndroidXmlString(String sourcePath, String outPath, String outFileName, String from, String to) {
        if (sourcePath == null || outPath == null || outFileName == null || from == null || to == null) {
            return;
        }
        KYouDaoHttp youdao = new KYouDaoHttp();
        youdao.setJava(true);
        // 解析xml
        SAXReader reader = new SAXReader();
        try {
            // 通过reader对象的read方法加载books.xml文件,获取docuemnt对象。
            Document document = reader.read(new File(sourcePath));
            // 通过document对象获取根节点bookstore
            Element rootElement = document.getRootElement();
            // 通过element对象的elementIterator方法获取迭代器
            Iterator it = rootElement.elementIterator();
            final int[] count = {1};//条目计数
            // 遍历迭代器，获取根节点中的信息
            while (it.hasNext()) {
                Element element = (Element) it.next();
                String name = element.attribute("name").getValue();
                String value = element.getStringValue();
//                System.out.println(name + "-" + value);
//                element.setText(translate.translate(value));
//                element.setText("1111");
                //fixme 翻译value
                final Boolean[] isFinish = {false};//判断是否翻译完成
                youdao.translate(value, from, to, new KYouDaoHttp.CallBack() {
                    @Override
                    public void onResponse(@NotNull String result) {
                        result = result.toLowerCase();//小写
                        System.out.println("翻译:\t" + (count[0]++) + "\t" + value + "\t" + result);
                        element.setText(result);//全部小写
                        isFinish[0] = true;
                    }
                });
                //fixme 等待回调完成，再继续往下执行。
                while (!isFinish[0]) {
                    try {
                        Thread.sleep(10);//线程休眠
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            //System.out.println(document.asXML());//输入全部文本
            File dirFile = new File(outPath);
            if (!dirFile.exists()) {
                dirFile.mkdirs();//防止输入目录不存在；
            }
            dirFile = null;
            FileWriter writer = new FileWriter(outPath + "//" + outFileName);
            document.write(writer);
            writer.flush();
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
