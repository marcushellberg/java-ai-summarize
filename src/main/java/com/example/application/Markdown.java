package com.example.application;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class Markdown extends Composite<Div> {

    Parser parser = Parser.builder().build();
    HtmlRenderer renderer = HtmlRenderer.builder().build();

    public Markdown() {
    }

    public Markdown(String markdown) {
        setMarkdown(markdown);
    }

    public void setMarkdown(String markdown) {
        getContent().getElement().setProperty("innerHTML",
                renderer.render(parser.parse(markdown))
        );
    }
}
