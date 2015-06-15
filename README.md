pinyinTokenFilter
=================

Apache Solr搜索引擎插件，用于将为中文词元增加拼音注解词元，采用[hanlp](https://github.com/hankcs/HanLP)进行拼音转换，支持多音字识别，如：“重阳” ==>> “chongyang”，“重量” ==>> “zhongliang”

在Solr 4.10.1版本中测试通过。

Usage/用法
-----------------

#Example/示例  

    <fieldType name="text_pinyin" class="solr.TextField">
          <analyzer type="index">
            <tokenizer class="solr.WhitespaceTokenizerFactory"/>
            <filter class="xyz.dowenliu.lucene.analyzer.PinyinTransformTokenFilterFactory"
                    isOutChinese="true" minTermLength="1" type="3"/>
            <filter class="solr.StopFilterFactory" ignoreCase="true"
                    words="stopwords.txt" />
            <filter class="solr.LowerCaseFilterFactory" />
            <filter class="solr.RemoveDuplicatesTokenFilterFactory"/>
          </analyzer>
          <analyzer type="query">
            <tokenizer class="solr.WhitespaceTokenizerFactory"/>
            <filter class="solr.LowerCaseFilterFactory" />
          </analyzer>
        </fieldType>

#Filter Class/过滤器类

*PinyinTransformTokenFilterFactory*

#Configuration/配置项
##isOutChinese
是否保留原输入中文词元。可选值：*true*(默认)/*false*

##type
拼音类型。可选值：*1* 全拼/*2* 拼音首字母/*3* 全部

##minTermLength
仅输出字数大于或等于*minTermLenght*的中文词元的拼音结果。默认值为2。