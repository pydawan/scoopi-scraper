package org.codetab.scoopi.step.parse.htmlunit;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;
import static org.codetab.scoopi.util.Util.spaceit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

import org.apache.commons.validator.routines.UrlValidator;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.helper.DocumentHelper;
import org.codetab.scoopi.step.base.BaseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Parser extends BaseParser {

    static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    @Inject
    private ValueParser htmlUnitValueParser;
    @Inject
    private DocumentHelper documentHelper;
    @Inject
    private ErrorLogger errorLogger;

    private static final int TIMEOUT_MILLIS = 120000;

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
            HtmlPage page = HTMLParser.parseHtml(response,
                    webClient.getCurrentWindow());

            htmlUnitValueParser.setPage(page);
            setValueParser(htmlUnitValueParser);
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
            String message =
                    spaceit("use default value:", String.valueOf(timeout));
            errorLogger.log(CAT.INTERNAL, message, e);
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
}
