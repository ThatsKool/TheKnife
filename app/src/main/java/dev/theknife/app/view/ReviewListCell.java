/*
 * Cella personalizzata per la visualizzazione delle recensioni nel dettaglio ristorante.
 */
package dev.theknife.app.view;

import dev.theknife.app.model.Review;
import dev.theknife.app.util.AnimationUtils;
import dev.theknife.app.viewmodel.RestaurantDetailsViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * ListCell per recensioni con azioni modifica/elimina e risposte ristoratore/cliente.
 */
public class ReviewListCell extends ListCell<Review> {

    private final RestaurantDetailsViewModel viewModel;
    private final Consumer<Review> onEditReview;
    private final Consumer<Review> onDeleteReview;
    private final Consumer<Review> onRestaurateurRespond;
    private final Consumer<Review> onClientRespond;
    private final Supplier<Boolean> hasEditAction;
    private final Supplier<Boolean> hasDeleteAction;

    public ReviewListCell(RestaurantDetailsViewModel viewModel,
                          Consumer<Review> onEditReview,
                          Consumer<Review> onDeleteReview,
                          Consumer<Review> onRestaurateurRespond,
                          Consumer<Review> onClientRespond,
                          Supplier<Boolean> hasEditAction,
                          Supplier<Boolean> hasDeleteAction) {
        this.viewModel = viewModel;
        this.onEditReview = onEditReview;
        this.onDeleteReview = onDeleteReview;
        this.onRestaurateurRespond = onRestaurateurRespond;
        this.onClientRespond = onClientRespond;
        this.hasEditAction = hasEditAction;
        this.hasDeleteAction = hasDeleteAction;
    }

    @Override
    protected void updateItem(Review review, boolean empty) {
        super.updateItem(review, empty);

        if (empty || review == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        VBox cellContent = new VBox();
        cellContent.setSpacing(8);
        cellContent.setPadding(new Insets(10));

        HBox header = new HBox();
        header.setSpacing(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String authorDisplay = viewModel.getReviewAuthorDisplayName(review);
        Label userLabel = new Label(authorDisplay);
        userLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        userLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label ratingLabel = new Label(review.getRatingStars());
        ratingLabel.setFont(Font.font("Arial", 14));
        ratingLabel.setStyle("-fx-text-fill: #e67e22;");

        Label dateLabel = new Label(review.getFormattedDate());
        dateLabel.setFont(Font.font("Arial", 10));
        dateLabel.setStyle("-fx-text-fill: #6c757d;");

        header.getChildren().addAll(userLabel, ratingLabel, dateLabel);

        Text commentText = new Text(review.getComment());
        commentText.setFont(Font.font("Arial", 12));
        commentText.setStyle("-fx-fill: #212529;");
        commentText.setWrappingWidth(450);

        cellContent.getChildren().addAll(header, commentText);

        if (review.hasRestaurateurResponse()) {
            VBox responseBox = new VBox();
            responseBox.setSpacing(5);
            responseBox.setPadding(new Insets(8));
            responseBox.setStyle(
                "-fx-background-color: #e8f4f8; -fx-border-color: #17a2b8; "
                    + "-fx-border-radius: 5; -fx-background-radius: 5;");

            Label responseHeader = new Label("Risposta dal Ristorante:");
            responseHeader.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            responseHeader.setStyle("-fx-text-fill: #17a2b8;");

            Text responseText = new Text(review.getRestaurateurResponse());
            responseText.setFont(Font.font("Arial", 11));
            responseText.setStyle("-fx-fill: #212529;");
            responseText.setWrappingWidth(430);

            responseBox.getChildren().addAll(responseHeader, responseText);

            if (review.hasClientResponse()) {
                VBox clientResponseBox = new VBox();
                clientResponseBox.setSpacing(5);
                clientResponseBox.setPadding(new Insets(8));
                clientResponseBox.setStyle(
                    "-fx-background-color: #fff3cd; -fx-border-color: #ffc107; "
                        + "-fx-border-radius: 5; -fx-background-radius: 5;");

                Label clientResponseHeader = new Label("Risposta da " + authorDisplay + ":");
                clientResponseHeader.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                clientResponseHeader.setStyle("-fx-text-fill: #856404;");

                Text clientResponseText = new Text(review.getClientResponse());
                clientResponseText.setFont(Font.font("Arial", 11));
                clientResponseText.setStyle("-fx-fill: #212529;");
                clientResponseText.setWrappingWidth(410);

                clientResponseBox.getChildren().addAll(clientResponseHeader, clientResponseText);
                responseBox.getChildren().add(clientResponseBox);
            }

            cellContent.getChildren().add(responseBox);
        }

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        boolean isReviewOwner = viewModel.isReviewOwner(review);
        boolean editConfigured = hasEditAction != null && Boolean.TRUE.equals(hasEditAction.get());
        boolean deleteConfigured = hasDeleteAction != null && Boolean.TRUE.equals(hasDeleteAction.get());

        if (isReviewOwner && (editConfigured || deleteConfigured)) {
            if (editConfigured && onEditReview != null) {
                Button editButton = new Button("Modifica");
                editButton.setStyle(
                    "-fx-background-color: #007bff; -fx-text-fill: white; "
                        + "-fx-padding: 4 12; -fx-background-radius: 4; -fx-font-size: 11;");
                editButton.setOnAction(e -> {
                    e.consume();
                    onEditReview.accept(review);
                });
                buttonBox.getChildren().add(editButton);
            }

            if (deleteConfigured && onDeleteReview != null) {
                Button deleteButton = new Button("Elimina");
                deleteButton.setStyle(
                    "-fx-background-color: #dc3545; -fx-text-fill: white; "
                        + "-fx-padding: 4 12; -fx-background-radius: 4; -fx-font-size: 11;");
                deleteButton.setOnAction(e -> {
                    e.consume();
                    onDeleteReview.accept(review);
                });
                buttonBox.getChildren().add(deleteButton);
            }
        }

        if (viewModel.canShowRestaurateurRespondButton(review) && onRestaurateurRespond != null) {
            Button respondButton = new Button("Rispondi");
            respondButton.setStyle(
                "-fx-background-color: #28a745; -fx-text-fill: white; "
                    + "-fx-padding: 4 12; -fx-background-radius: 4; -fx-font-size: 11;");
            respondButton.setOnAction(e -> {
                e.consume();
                onRestaurateurRespond.accept(review);
            });
            buttonBox.getChildren().add(respondButton);
        }

        if (viewModel.canShowClientRespondButton(review) && onClientRespond != null) {
            Button respondButton = new Button("Rispondi");
            respondButton.setStyle(
                "-fx-background-color: #ffc107; -fx-text-fill: #212529; "
                    + "-fx-padding: 4 12; -fx-background-radius: 4; -fx-font-size: 11;");
            respondButton.setOnAction(e -> {
                e.consume();
                onClientRespond.accept(review);
            });
            buttonBox.getChildren().add(respondButton);
        }

        if (!buttonBox.getChildren().isEmpty()) {
            cellContent.getChildren().add(buttonBox);
        }

        setGraphic(cellContent);
        setText(null);
        AnimationUtils.slideInFromBottom(cellContent, 300, 50);
    }
}
