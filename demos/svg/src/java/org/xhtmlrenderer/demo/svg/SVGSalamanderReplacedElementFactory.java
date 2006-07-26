package org.xhtmlrenderer.demo.svg;

import com.kitfox.svg.SVGException;
import com.kitfox.svg.app.beans.SVGPanel;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.swing.SwingReplacedElement;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import java.util.logging.Level;
import java.awt.*;

/**
 * Factory to create ReplacedElements for SVG embedded in our XML file, using
 * the Salamander library. Salamander in this case will return a Swing JPanel.
 */
public class SVGSalamanderReplacedElementFactory implements ReplacedElementFactory {

    public ReplacedElement createReplacedElement(
            LayoutContext c,
            BlockBox box,
            UserAgentCallback uac,
            int cssWidth,
            int cssHeight) {

        SVGPanel panel = new SVGPanel();
        String content = null;
        JComponent cc = null;
        try {
            Element elem = box.element;
            if (elem == null || ! isSVGEmbedded(elem)) {
                return null;
            }

            // HACK: the easiest way to integrate with Salamander is to have it read
            // our SVG from a file--so, push the content to a temporary file, yuck!
            content = elem.getFirstChild().getNodeValue();

            String path = elem.getAttribute("data");
            XRLog.general(Level.FINE, "Rendering embedded SVG via object tag from: " + path);
            XRLog.general(Level.FINE, "Content is: " + content);
            panel.setAntiAlias(true);
            panel.setSvgResourcePath(path);

            int width = panel.getSVGWidth();
            int height = panel.getSVGHeight();

            if ( cssWidth > 0 ) width = cssWidth;

            if ( cssHeight > 0 ) height = cssHeight;

            String val = elem.getAttribute("width");
            if ( val != null && val.length() > 0 ) {
                width = Integer.valueOf(val).intValue();
            }
            val = elem.getAttribute("height");
            if ( val != null && val.length() > 0 ) {
                height = Integer.valueOf(val).intValue();
            }
            panel.setScaleToFit(true);
            panel.setPreferredSize(new Dimension(width, height));
            panel.setSize(panel.getPreferredSize());

            cc = panel;
        } catch (SVGException e) {
            XRLog.general(Level.WARNING, "Could not replace SVG element; rendering failed" +
                    " in SVG renderer. Skipping and using blank JPanel.", e);
            cc = getDefaultJComponent(content, cssWidth, cssHeight);
        }
        if (cc == null) {
            return null;
        } else {
            SwingReplacedElement result = new SwingReplacedElement(cc);
            if (c.isInteractive()) {
                c.getCanvas().add(cc);
            }
            return result;
        }
    }

    private boolean isSVGEmbedded(Element elem) {
        return elem.getNodeName().equals("object") && elem.getAttribute("type").equals("image/svg+xml");
    }

    private JComponent getDefaultJComponent(String content, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel comp = new JLabel(content);
        panel.add(comp, BorderLayout.CENTER);
        panel.setOpaque(false);
        if ( width > 0 && height > 0 ) {
            panel.setPreferredSize(new Dimension(width, height));
            panel.setSize(panel.getPreferredSize());
        } else {
            panel.setPreferredSize(comp.getPreferredSize());
            panel.setSize(comp.getPreferredSize());
        }
        return panel;
    }
}