package me.sirtyler.pokedex.gui;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class AnimatedGIF {

    private List<ImageFrame> frames;
    private int frame;
    private Timer playTimer;
    private JComponent player;

    public AnimatedGIF(JComponent value) {
        this.player = value;
        frames = new ArrayList<>(25);
        playTimer = new Timer(0, e -> {
            frame++;
            if (frame >= frames.size()) {
                frame = 0;
            }
            player.repaint();
            playTimer.setDelay(frames.get(0).getGraphicControlExtension().getDelayTime());
        });
    }

    public AnimatedGIF(JComponent player, URLConnection connection) throws IOException {
        this(player);
        try (InputStream is = connection.getInputStream(); ImageInputStream stream = ImageIO.createImageInputStream(is)) {
            Iterator readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new RuntimeException("no image reader found");
            }
            ImageReader reader = (ImageReader) readers.next();
            reader.setInput(stream);            // don't omit this line!
            int n = reader.getNumImages(true);  // don't use false!
            System.out.println("numImages = " + n);
            for (int i = 0; i < n; i++) {
                BufferedImage image = reader.read(i);
                ImageFrame imageFrame = new ImageFrame(image);

                IIOMetadata imd = reader.getImageMetadata(i);
                Node tree = imd.getAsTree("javax_imageio_gif_image_1.0");
                NodeList children = tree.getChildNodes();

                for (int j = 0; j < children.getLength(); j++) {
                    Node nodeItem = children.item(j);
                    NamedNodeMap attr = nodeItem.getAttributes();
                    switch (nodeItem.getNodeName()) {
                        case "ImageDescriptor":
                            ImageDescriptor id = new ImageDescriptor(
                                    getIntValue(attr.getNamedItem("imageLeftPosition")),
                                    getIntValue(attr.getNamedItem("imageTopPosition")),
                                    getIntValue(attr.getNamedItem("imageWidth")),
                                    getIntValue(attr.getNamedItem("imageHeight")),
                                    getBooleanValue(attr.getNamedItem("interlaceFlag")));
                            imageFrame.setImageDescriptor(id);
                            break;
                        case "GraphicControlExtension":
                            GraphicControlExtension gc = new GraphicControlExtension(
                                    DisposalMethod.find(getNodeValue(attr.getNamedItem("disposalMethod"))),
                                    getBooleanValue(attr.getNamedItem("userInputFlag")),
                                    getBooleanValue(attr.getNamedItem("transparentColorFlag")),
                                    getIntValue(attr.getNamedItem("delayTime")) * 10,
                                    getIntValue(attr.getNamedItem("transparentColorIndex")));
                            imageFrame.setGraphicControlExtension(gc);
                            break;
                    }
                }
                frames.add(imageFrame);
            }
        }
    }

    public AnimatedGIF(JComponent player, URL url) throws IOException {
        this(player);
        try (InputStream is = url.openStream(); ImageInputStream stream = ImageIO.createImageInputStream(is)) {
            Iterator readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new RuntimeException("no image reader found");
            }
            ImageReader reader = (ImageReader) readers.next();
            reader.setInput(stream);            // don't omit this line!
            int n = reader.getNumImages(true);  // don't use false!
            System.out.println("numImages = " + n);
            for (int i = 0; i < n; i++) {
                BufferedImage image = reader.read(i);
                ImageFrame imageFrame = new ImageFrame(image);

                IIOMetadata imd = reader.getImageMetadata(i);
                Node tree = imd.getAsTree("javax_imageio_gif_image_1.0");
                NodeList children = tree.getChildNodes();

                for (int j = 0; j < children.getLength(); j++) {
                    Node nodeItem = children.item(j);
                    NamedNodeMap attr = nodeItem.getAttributes();
                    switch (nodeItem.getNodeName()) {
                        case "ImageDescriptor":
                            ImageDescriptor id = new ImageDescriptor(
                                    getIntValue(attr.getNamedItem("imageLeftPosition")),
                                    getIntValue(attr.getNamedItem("imageTopPosition")),
                                    getIntValue(attr.getNamedItem("imageWidth")),
                                    getIntValue(attr.getNamedItem("imageHeight")),
                                    getBooleanValue(attr.getNamedItem("interlaceFlag")));
                            imageFrame.setImageDescriptor(id);
                            break;
                        case "GraphicControlExtension":
                            GraphicControlExtension gc = new GraphicControlExtension(
                                    DisposalMethod.find(getNodeValue(attr.getNamedItem("disposalMethod"))),
                                    getBooleanValue(attr.getNamedItem("userInputFlag")),
                                    getBooleanValue(attr.getNamedItem("transparentColorFlag")),
                                    getIntValue(attr.getNamedItem("delayTime")) * 10,
                                    getIntValue(attr.getNamedItem("transparentColorIndex")));
                            imageFrame.setGraphicControlExtension(gc);
                            break;
                    }
                }
                frames.add(imageFrame);
            }
        }
    }

    public BufferedImage getCurrentFrame() {
        // If this was a optimised GIF, we would need to be
        // merging frames together to produce the current frame
        // This would then need to be reset each time we cycle...
        return frames.isEmpty() ? null : frames.get(frame).getImage();
    }

    public void play() {
        if (!frames.isEmpty()) {
            frame = 0;
            playTimer.setDelay(frames.get(0).getGraphicControlExtension().getDelayTime());
            playTimer.start();
            player.repaint();
        }
    }

    public void stop() {
        playTimer.stop();
    }

    protected String getNodeValue(Node node) {
        return node == null ? null : node.getNodeValue();
    }

    protected int getIntValue(Node node) {
        return node == null ? 0 : getIntValue(node.getNodeValue());
    }

    protected boolean getBooleanValue(Node node) {
        return node != null && getBooleanValue(node.getNodeValue());
    }

    protected int getIntValue(String value) {
        return value == null ? 0 : Integer.parseInt(value);
    }

    protected boolean getBooleanValue(String value) {
        return value != null && Boolean.parseBoolean(value);
    }

    public enum DisposalMethod {
        RESTORE_TO_BACKGROUND,
        RESTORE_TO_PREVIOUS,
        DO_NOT_DISPOSE,
        UNSPECIFIED;

        public static DisposalMethod find(String text) {

            DisposalMethod dm = UNSPECIFIED;

            switch (text) {
                case "restoreToBackgroundColor":
                    dm = RESTORE_TO_BACKGROUND;
                    break;
            }

            return dm;

        }
    }

    public class ImageFrame {

        private BufferedImage image;
        private ImageDescriptor imageDescriptor;
        private GraphicControlExtension graphicControlExtension;

        public ImageFrame(BufferedImage image) {
            this.image = image;
        }

        public GraphicControlExtension getGraphicControlExtension() {
            return graphicControlExtension;
        }

        protected void setGraphicControlExtension(GraphicControlExtension graphicControlExtension) {
            this.graphicControlExtension = graphicControlExtension;
        }

        public BufferedImage getImage() {
            return image;
        }

        public ImageDescriptor getImageDescriptor() {
            return imageDescriptor;
        }

        protected void setImageDescriptor(ImageDescriptor imageDescriptor) {
            this.imageDescriptor = imageDescriptor;
        }

    }

    public class GraphicControlExtension {

        private DisposalMethod disposalMethod;
        private boolean userInputFlag;
        private boolean transparentColorFlag;
        private int delayTime;
        private int transparentColorIndex;

        public GraphicControlExtension(DisposalMethod disposalMethod, boolean userInputFlag, boolean transparentColorFlag, int delayTime, int transparentColorIndex) {
            this.disposalMethod = disposalMethod;
            this.userInputFlag = userInputFlag;
            this.transparentColorFlag = transparentColorFlag;
            this.delayTime = delayTime;
            this.transparentColorIndex = transparentColorIndex;
        }

        public int getDelayTime() {
            return delayTime;
        }

        public DisposalMethod getDisposalMethod() {
            return disposalMethod;
        }

        public int getTransparentColorIndex() {
            return transparentColorIndex;
        }

        public boolean isTransparentColorFlag() {
            return transparentColorFlag;
        }

        public boolean isUserInputFlag() {
            return userInputFlag;
        }

    }

    public class ImageDescriptor {

        private int imageLeftPosition;
        private int imageTopPosition;
        private int imageHeight;
        private int imageWeight;
        private boolean interlaced;

        public ImageDescriptor(int imageLeftPosition, int imageTopPosition, int imageHeight, int imageWeight, boolean interlaced) {
            this.imageLeftPosition = imageLeftPosition;
            this.imageTopPosition = imageTopPosition;
            this.imageHeight = imageHeight;
            this.imageWeight = imageWeight;
            this.interlaced = interlaced;
        }

        public int getImageHeight() {
            return imageHeight;
        }

        public int getImageLeftPosition() {
            return imageLeftPosition;
        }

        public int getImageTopPosition() {
            return imageTopPosition;
        }

        public int getImageWeight() {
            return imageWeight;
        }

        public boolean isInterlaced() {
            return interlaced;
        }

    }

}