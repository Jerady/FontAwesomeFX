package de.jensd.fx.glyphs.demo.browser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;

public class Demo extends Application {


    Image image = new Image("http://upload.wikimedia.org/wikipedia/commons/thumb/4/41/Siberischer_tiger_de_edit02.jpg/320px-Siberischer_tiger_de_edit02.jpg");

    SelectionModel selectionModel = new SelectionModel();

    @Override
    public void start(Stage primaryStage) {

        Pane pane = new Pane();

        double width = image.getWidth();
        double height = image.getHeight();

        double padding = 20;
        for( int row=0; row < 4; row++) {
            for( int col=0; col < 4; col++) {

                ImageView imageView = new ImageView( image);
                imageView.relocate( padding * (col+1) + width * col, padding * (row + 1) + height * row);

                pane.getChildren().add(imageView);

            }
        }

        Scene scene = new Scene( pane, 1800, 1200);

        primaryStage.setScene( scene);
        primaryStage.show();        

        new RubberBandSelection( pane);

    }

    public static void main(String[] args) {
        launch(args);
    }

    private class SelectionModel {

        Set<Node> selection = new HashSet<>();

        public void add( Node node) {
            node.setStyle("-fx-effect: dropshadow(three-pass-box, red, 10, 10, 0, 0);");
            selection.add( node);
        }

        public void remove( Node node) {
            node.setStyle("-fx-effect: null");
            selection.remove( node);
        }

        public void clear() {

            while( !selection.isEmpty()) {
                remove( selection.iterator().next());
            }

        }

        public boolean contains( Node node) {
            return selection.contains(node);
        }

        public void log() {
            System.out.println( "Items in model: " + Arrays.asList( selection.toArray()));
        }

    }

    private class RubberBandSelection {

        final DragContext dragContext = new DragContext();
        Rectangle rect;

        Pane group;

        public RubberBandSelection( Pane group) {

            this.group = group;

            rect = new Rectangle( 0,0,0,0);
            rect.setStroke(Color.BLUE);
            rect.setStrokeWidth(1);
            rect.setStrokeLineCap(StrokeLineCap.ROUND);
            rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));

            group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
            group.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);

        }

        EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                dragContext.mouseAnchorX = event.getSceneX();
                dragContext.mouseAnchorY = event.getSceneY();

                rect.setX(dragContext.mouseAnchorX);
                rect.setY(dragContext.mouseAnchorY);
                rect.setWidth(0);
                rect.setHeight(0);

                group.getChildren().add( rect);

                event.consume();

            }
        };

        EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                if( !event.isShiftDown() && !event.isControlDown()) {
                    selectionModel.clear();
                }

                for( Node node: group.getChildren()) {

                    if( node instanceof ImageView) {
                        if( node.getBoundsInParent().intersects( rect.getBoundsInParent())) {

                            if( event.isShiftDown()) {

                                selectionModel.add( node);

                            } else if( event.isControlDown()) {

                                if( selectionModel.contains( node)) {
                                    selectionModel.remove( node);
                                } else {
                                    selectionModel.add( node);
                                }
                            } else {
                                selectionModel.add( node);
                            }

                        }
                    }

                }

                selectionModel.log();

                rect.setX(0);
                rect.setY(0);
                rect.setWidth(0);
                rect.setHeight(0);

                group.getChildren().remove( rect);

                event.consume();

            }
        };

        EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                double offsetX = event.getSceneX() - dragContext.mouseAnchorX;
                double offsetY = event.getSceneY() - dragContext.mouseAnchorY;

                if( offsetX > 0)
                    rect.setWidth( offsetX);
                else {
                    rect.setX(event.getSceneX());
                    rect.setWidth(dragContext.mouseAnchorX - rect.getX());
                }

                if( offsetY > 0) {
                    rect.setHeight( offsetY);
                } else {
                    rect.setY(event.getSceneY());
                    rect.setHeight(dragContext.mouseAnchorY - rect.getY());
                }

                event.consume();

            }
        };

        private final class DragContext {

            public double mouseAnchorX;
            public double mouseAnchorY;


        }
    }
}