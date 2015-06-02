package xyz.dowenliu.lucene.analyzer;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.util.Map;

/**
 * 拼音转换分词过滤器工厂类
 *
 * @author liufl / 2014年7月2日
 */
public class PinyinTransformTokenFilterFactory extends TokenFilterFactory {

    private boolean isOutChinese = true; // 是否输出原中文开关
    private int minTermLength = 2; // 中文词组长度过滤，默认超过2位长度的中文才转换拼音
    private int type = 1; // 拼音类型，1 全拼，2 首字母，3 全部

    /**
     * 构造器
     */
    public PinyinTransformTokenFilterFactory(Map<String, String> args) {
        super(args);
        this.isOutChinese = getBoolean(args, "isOutChinese", true);
        this.type = getInt(args, "type", 1);
        this.minTermLength = getInt(args, "minTermLength", 2);
        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    public TokenFilter create(TokenStream input) {
        return new PinyinTransformTokenFilter(input, this.type,
                this.minTermLength, this.isOutChinese);
    }
}
