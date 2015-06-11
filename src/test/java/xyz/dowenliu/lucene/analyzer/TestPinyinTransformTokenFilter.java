package xyz.dowenliu.lucene.analyzer;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.MockTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

public class TestPinyinTransformTokenFilter extends BaseTokenStreamTestCase {

    private MockTokenizer tokenizer;
    private PinyinTransformTokenFilter filter;

    @Before
    public void before() throws IOException {
        this.tokenizer = new MockTokenizer();
        this.tokenizer.setReader(new StringReader("中華人民共和國凪のあすから我们，。"));
    }

    @Test
    public void testFull() throws IOException {
        this.filter = new PinyinTransformTokenFilter(tokenizer, 3, 1);
        this.filter.reset();
        int position = 0;
        while (this.filter.incrementToken()) {
            CharTermAttribute termAtt = this.filter.getAttribute(CharTermAttribute.class);
            String token = termAtt.toString();
            int increment = this.filter.getAttribute(PositionIncrementAttribute.class).getPositionIncrement();
            position += increment;
            OffsetAttribute offset = this.filter.getAttribute(OffsetAttribute.class);
            TypeAttribute type = this.filter.getAttribute(TypeAttribute.class);
            System.out.println(position + "[" + offset.startOffset() + "," + offset.endOffset() + "} (" + type
                    .type() + ") " + token);
        }
    }

}
