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

  @Before
  public void before() throws IOException {
  }

  @Test
  public void testFull() throws IOException {
    String[] arr = new String[]{"流浪者之歌", "麦基", "吸烟有害健康", "明天会更好", "CBSi",};
    for (String s : arr) {
      System.out.println(s + "===============");
      MockTokenizer tokenizer = new MockTokenizer();
      tokenizer.setReader(new StringReader(s));
      PinyinTransformTokenFilter filter = new PinyinTransformTokenFilter(tokenizer, 3, 1);
      filter.reset();
      int position = 0;
      while (filter.incrementToken()) {
        CharTermAttribute termAtt = filter.getAttribute(CharTermAttribute.class);
        String token = termAtt.toString();
        int increment = filter.getAttribute(PositionIncrementAttribute.class).getPositionIncrement();
        position += increment;
        OffsetAttribute offset = filter.getAttribute(OffsetAttribute.class);
        TypeAttribute type = filter.getAttribute(TypeAttribute.class);
        System.out.println(position + "[" + offset.startOffset() + "," + offset.endOffset() + "} (" + type
            .type() + ") " + token);
      }
    }
  }

}
