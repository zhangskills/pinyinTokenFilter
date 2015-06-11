package xyz.dowenliu.lucene.analyzer;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 拼音转换分词过滤器
 *
 * @author liufl / 2014年7月1日
 */
public class PinyinTransformTokenFilter extends TokenFilter {

    private boolean isOutChinese = true; // 是否输出原中文开关
    private int type = 1; // 拼音类型，1 全拼，2 首字母，3 全部
    private int _minTermLength = 2; // 中文词组长度过滤，默认超过2位长度的中文才转换拼音

    private HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat(); // 拼音转接输出格式

    private char[] curTermBuffer; // 底层词元输入缓存
    private int curTermLength; // 底层词元输入长度

    private final CharTermAttribute termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class); // 词元记录
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class); // 位置增量属性
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class); // 类型属性
    private boolean hasCurOut = false; // 当前输入是否已输出
    private Collection<String> terms = null; // 拼音结果集
    private Iterator<String> termIte = null; // 拼音结果集迭代器

    /**
     * 构造器。默认长度超过2的中文词元进行转换，转换为全拼音且保留原中文词元
     *
     * @param input 词元输入
     */
    public PinyinTransformTokenFilter(TokenStream input) {
        this(input, 1, 2);
    }

    /**
     * 构造器。默认保留原中文词元
     *
     * @param input         词元输入
     * @param type     输出拼音缩写还是完整拼音
     * @param minTermLength 中文词组过滤长度
     */
    public PinyinTransformTokenFilter(TokenStream input, int type,
            int minTermLength) {
        this(input, type, minTermLength, true);
    }

    /**
     * 构造器
     *
     * @param input         词元输入
     * @param type     输出拼音缩写还是完整拼音
     * @param minTermLength 中文词组过滤长度
     * @param isOutChinese  是否输入原中文词元
     */
    public PinyinTransformTokenFilter(TokenStream input, int type,
            int minTermLength, boolean isOutChinese) {
        super(input);
        this._minTermLength = minTermLength;
        if (this._minTermLength < 1) {
            this._minTermLength = 1;
        }
        this.isOutChinese = isOutChinese;
        this.outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        this.outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        this.type = type;
        addAttribute(OffsetAttribute.class); // 偏移量属性
    }

    /**
     * 判断字符串中是否含有中文
     *
     * @param s 待检测文本
     * @return 中文字符数
     */
    public static int chineseCharCount(String s) {
        int count = 0;
        if ((null == s) || ("".equals(s.trim()))) {
            return count;
        }
        for (int i = 0; i < s.length(); i++) {
            if (isChinese(s.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    /**
     * 判断字符是否是中文
     *
     * @param a 待测字符
     * @return 是 {@code true} ；否 {@code false}
     */
    public static boolean isChinese(char a) {
        //[\u4E00-\u9FFF]
        return a >= '\u4e00' && a <= '\u9fff';
//        return ((int) a >= 19968) && ((int) a <= 171941);
    }

    /**
     * 分词过滤。<br/>
     * 该方法在上层调用中被循环调用，直到该方法返回false
     */
    public final boolean incrementToken() throws IOException {
        while (true) {
            if (this.curTermBuffer == null) { // 开始处理或上一输入词元已被处理完成
                if (!this.input.incrementToken()) { // 获取下一词元输入
                    return false; // 没有后继词元输入，处理完成，返回false，结束上层调用
                }
                // 缓存词元输入
                this.curTermBuffer = this.termAtt.buffer().clone();
                this.curTermLength = this.termAtt.length();
            }
            // 处理原输入词元
            if ((this.isOutChinese) && (!this.hasCurOut) && (this.termIte == null)) {
                // 准许输出原中文词元且当前没有输出原输入词元且还没有处理拼音结果集
                this.hasCurOut = true; // 标记以保证下次循环不会输出
                // 写入原输入词元
                this.termAtt.copyBuffer(this.curTermBuffer, 0,
                        this.curTermLength);
                this.posIncrAtt.setPositionIncrement(this.posIncrAtt.getPositionIncrement());
                return true; // 继续
            }
            String chinese = this.termAtt.toString();
            // 拼音处理
            if (chineseCharCount(chinese) >= this._minTermLength) {
                //有中文且符合长度限制
                try {
                    // 输出拼音（缩写或全拼）
                    if (this.type == 1) {
                        this.terms = GetPyString(chinese);
                    }else if (this.type == 2) {
                        this.terms = getPyShort(chinese);
                    } else {
                        Collection<String> list = GetPyString(chinese);
                        if (list == null) {
                            list = getPyShort(chinese);
                        } else {
                            list.addAll(getPyShort(chinese));
                        }
                        this.terms = list;
                    }
                    if (this.terms != null) {
                        this.termIte = this.terms.iterator();
                    }
                } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                    badHanyuPinyinOutputFormatCombination.printStackTrace();
                }

            }
            if (this.termIte != null) {
                if (this.termIte.hasNext()) { // 有拼音结果集且未处理完成
                    String pinyin = this.termIte.next();
                    this.termAtt.copyBuffer(pinyin.toCharArray(), 0, pinyin.length());
                    this.posIncrAtt.setPositionIncrement(0);
                    this.typeAtt.setType("pinyin");
                    return true;
                }
            }
            // 没有中文或转换拼音失败，不用处理，
            // 清理缓存，下次取新词元
            this.curTermBuffer = null;
            this.termIte = null;
            this.hasCurOut = false; // 下次取词元后输出原词元（如果开关也准许）
        }
    }

    /**
     * 获取拼音缩写
     *
     * @param chinese 含中文的字符串，若不含中文，原样输出
     * @return 转换后的文本
     * @throws BadHanyuPinyinOutputFormatCombination
     */
    private Collection<String> getPyShort(String chinese)
            throws BadHanyuPinyinOutputFormatCombination {
        List<String[]> pinyinList = new ArrayList<>();
        for (int i = 0; i < chinese.length(); i++) {
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(
                    chinese.charAt(i), this.outputFormat);
            if (pinyinArray != null && pinyinArray.length > 0) {
                pinyinList.add(pinyinArray);
            }
        }
        Set<String> pinyins = new HashSet<>();
        for (String[] array : pinyinList) {
            if (pinyins.isEmpty()) {
                for (String charPinpin : array) {
                    pinyins.add(charPinpin.substring(0, 1));
                }
            } else {
                Set<String> pres = pinyins;
                pinyins = new HashSet<>();
                for (String pre : pres) {
                    for (String charPinyin : array) {
                        pinyins.add(pre + charPinyin.substring(0, 1));
                    }
                }
            }
        }
        return pinyins;
    }

    public void reset() throws IOException {
        super.reset();
    }

    /**
     * 获取拼音
     *
     * @param chinese 含中文的字符串，若不含中文，原样输出
     * @return 转换后的文本
     * @throws BadHanyuPinyinOutputFormatCombination
     */
    private Collection<String> GetPyString(String chinese)
            throws BadHanyuPinyinOutputFormatCombination {
        List<String[]> pinyinList = new ArrayList<>();
        for (int i = 0; i < chinese.length(); i++) {
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(
                    chinese.charAt(i), this.outputFormat);
            if (pinyinArray != null && pinyinArray.length > 0) {
                pinyinList.add(pinyinArray);
            }
        }
        Set<String> pinyins = null;
        for (String[] array : pinyinList) {
            if (pinyins == null || pinyins.isEmpty()) {
                pinyins = new HashSet<>();
                Collections.addAll(pinyins, array);
            } else {
                Set<String> pres = pinyins;
                pinyins = new HashSet<>();
                for (String pre : pres) {
                    for (String charPinyin : array) {
                        pinyins.add(pre + charPinyin);
                    }
                }
            }
        }
        return pinyins;
    }
}
