package com.example.application;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.apache.tika.exception.WriteLimitReachedException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.ai.chat.client.ChatClient;

import java.io.File;
import java.io.InputStream;

@Route("")
public class SummarizeView extends VerticalLayout {

    private final ChatClient chatClient;


    private final Div output = new Div();

    public SummarizeView(ChatClient.Builder chatClientBuilder) {
        chatClient = chatClientBuilder
                .defaultSystem("""
                        Summarize the following text into a concise paragraph that captures the main points and essential details without losing important information. 
                        The summary should be as short as possible while remaining clear and informative.
                        Use bullet points or numbered lists to organize the information if it helps to clarify the meaning. 
                        Focus on the key facts, events, and conclusions. 
                        Avoid including minor details or examples unless they are crucial for understanding the main ideas.
                        """)
                .build();
        createUI();
    }

    private void createUI() {
        var fb = new FileBuffer();
        var upload = new Upload();
        upload.setReceiver(fb);
        upload.addSucceededListener(e -> {
            var tmpFile = fb.getFileData().getFile();
            var fileName = e.getFileName();
            parseFile(tmpFile, fileName);
            tmpFile.delete();
        });

        var heading = new H1("Summarize Anything!");
        heading.addClassName(LumoUtility.FontSize.XLARGE);
        var header = new HorizontalLayout(heading, upload);
        header.setAlignItems(Alignment.BASELINE);
        header.addClassName(LumoUtility.FlexWrap.WRAP);

        add(header, output);
    }

    private void parseFile(File tmpFile, String fileName) {
        var parser = new AutoDetectParser();
        var handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);
        metadata.set("File size", tmpFile.length() + "B");

        try (InputStream stream = TikaInputStream.get(tmpFile)) {
            parser.parse(stream, handler, metadata);
            summarizeFile(handler.toString());
        } catch (WriteLimitReachedException ex) {
            Notification.show(ex.getMessage());
            summarizeFile(handler.toString());
        } catch (Exception ex) {
            output.add(new H2("Parsing Data failed: " + ex.getMessage()));
            throw new RuntimeException(ex);
        }
    }

    private void summarizeFile(String content) {
        var markdown = chatClient.prompt()
                .user("Text to summarize: " + content)
                .call()
                .content();

        output.removeAll();
        output.add(new Markdown(markdown));
    }

}
