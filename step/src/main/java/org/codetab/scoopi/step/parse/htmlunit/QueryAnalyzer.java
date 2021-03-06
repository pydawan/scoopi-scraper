package org.codetab.scoopi.step.parse.htmlunit;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

import org.apache.commons.validator.routines.UrlValidator;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.helper.DocumentHelper;
import org.codetab.scoopi.step.base.BaseQueryAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class QueryAnalyzer extends BaseQueryAnalyzer {

    static final Logger LOGGER = LoggerFactory.getLogger(QueryAnalyzer.class);

    @Inject
    private DocumentHelper documentHelper;

    private static final int TIMEOUT_MILLIS = 120000;

    private HtmlPage page;

    @Override
    protected boolean postInitialize() {
        notNull(document, "document must not be null");
        validState(nonNull(document.getDocumentObject()),
                "documentObject is not loaded");

        WebClient webClient = null;
        try {
            String html = getDocumentHTML();
            URL url = getDocumentURL();
            StringWebResponse response = new StringWebResponse(html, url);
            webClient = getWebClient();
            page = HTMLParser.parseHtml(response, webClient.getCurrentWindow());

            return true;
        } catch (IllegalStateException | IOException | DataFormatException e) {
            String message = "unable to initialize parser";
            throw new StepRunException(message, e);
        } finally {
            if (webClient != null) {
                webClient.setRefreshHandler(new ImmediateRefreshHandler());
                webClient.close();
            }
        }
    }

    private String getDocumentHTML() throws DataFormatException, IOException {
        byte[] bytes = documentHelper.getDocumentObject(document);
        return new String(bytes);
    }

    private WebClient getWebClient() {
        int timeout = TIMEOUT_MILLIS;
        String key = "scoopi.webClient.timeout"; //$NON-NLS-1$
        try {
            timeout = Integer.parseInt(configService.getConfig(key));
        } catch (NumberFormatException | ConfigNotFoundException e) {
        }

        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.setRefreshHandler(new ThreadedRefreshHandler());

        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setAppletEnabled(false);
        webClient.getOptions().setPopupBlockerEnabled(true);
        webClient.getOptions().setTimeout(timeout);
        return webClient;
    }

    private URL getDocumentURL() throws MalformedURLException {
        URL url;
        if (UrlValidator.getInstance().isValid(document.getUrl())) {
            url = new URL(document.getUrl());
        } else {
            url = new URL(new URL("file:"), document.getUrl()); //$NON-NLS-1$
        }
        return url;
    }

    @Override
    protected List<String> getQueryElements(final String xpath) {
        List<String> list = new ArrayList<>();
        try {
            List<Object> elements = page.getByXPath(xpath);
            elements.stream().forEach(e -> list.add(((DomNode) e).asXml()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return list;
    }

    @Override
    protected String getPageSource() {
        String pageSource = "";
        try {
            byte[] bytes = documentHelper.getDocumentObject(document);
            pageSource = new String(bytes);
        } catch (DataFormatException | IOException e) {
            LOGGER.error("", e);
        }
        return pageSource;
    }
}
