package com.edmunds.vinspy.nutch.parser;

import com.edmunds.vinspy.extractors.ExtractedAttr;
import com.edmunds.vinspy.extractors.ExtractionResults;
import com.edmunds.vinspy.extractors.ExtractionSource;
import com.edmunds.vinspy.extractors.ExtractionUtils;
import com.edmunds.vinspy.extractors.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseFilter;
import org.apache.nutch.storage.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

/**
 * @author zenind
 */
public class InventoryAttributesParser implements ParseFilter {

    public static final String TEMPLATE_PROPERTY = "vinspy.parse.template";

    private static final Logger LOG = LoggerFactory.getLogger(InventoryAttributesParser.class);

    private static final String VINSPY_PREFIX = "meta_vinspy-";

    private Configuration conf;

    public Parse filter(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) {
        String templateEncodedValue = conf.get(TEMPLATE_PROPERTY);
        if (StringUtils.isEmpty(templateEncodedValue)) {
            handleError(String.format("No template is specified for '%s' page", url), page, null);
        } else {
            try {
                Template template = ExtractionUtils.decodeTemplate(templateEncodedValue);
                String content = IOUtils.toString(new ByteArrayInputStream(page.getContent().array()), Charset.defaultCharset());
                ExtractionResults results = template.apply(new ExtractionSource(content, url));
                for (Map.Entry<ExtractedAttr, Object> attributeEntry : results.getAttrMap().entrySet()) {
                    if (attributeEntry.getValue() != null) {
                        String attributeFieldName = attribute(attributeEntry.getKey().name());
                        String value = String.valueOf(attributeEntry.getValue()).trim();
                        page.getMetadata().put(attributeFieldName, ByteBuffer.wrap(value.getBytes()));
                    }
                }
            } catch (Throwable e) {
                handleError(String.format("Issue during parsing '%s' page: %s", url, e), page, e);
            }
        }
        return parse;
    }

    String attribute(String name) {
        return VINSPY_PREFIX + name.toLowerCase();
    }

    private void handleError(String errorMessage, WebPage page, Throwable e) {
        LOG.error(errorMessage);
        page.getMetadata().put(attribute("error"), ByteBuffer.wrap(errorMessage.getBytes()));
        if (e != null) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            page.getMetadata().put(attribute("error-stacktrace"), ByteBuffer.wrap(sw.toString().getBytes()));
        }
    }

    @Override
    public Collection<WebPage.Field> getFields() {
        return null;
    }

    public void setConf(Configuration conf) {

        this.conf = conf;
    }

    public Configuration getConf() {
        return this.conf;
    }

}
