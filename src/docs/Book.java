package org.davidmoten.oa3.codegen.test.library.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Generated;
import jakarta.annotation.Nonnull;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.library.Globals;
import org.davidmoten.oa3.codegen.util.Util;

@JsonInclude(Include.NON_ABSENT)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        creatorVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.2.2-SNAPSHOT")
public final class Book {

    @JsonProperty("title")
    private final String title;

    @JsonProperty("author")
    private final PersonName author;

    @JsonProperty("abstract")
    private final Optional<Abstract> abstract_;

    @JsonProperty("publishedYear")
    private final Year publishedYear;

    @JsonProperty("authorId")
    private final Id authorId;

    @JsonProperty("isbn")
    private final ISBN isbn;

    @JsonProperty("itemId")
    private final ItemId itemId;

    @JsonProperty("language")
    private final Language language;

    @JsonCreator
    public Book(
            @Nonnull @JsonProperty("title") String title,
            @Nonnull @JsonProperty("author") PersonName author,
            @Nonnull @JsonProperty("abstract") Optional<Abstract> abstract_,
            @Nonnull @JsonProperty("publishedYear") Year publishedYear,
            @Nonnull @JsonProperty("authorId") Id authorId,
            @Nonnull @JsonProperty("isbn") ISBN isbn,
            @Nonnull @JsonProperty("itemId") ItemId itemId,
            @Nonnull @JsonProperty("language") Language language) {
        if (Globals.config().validateInConstructor().test(Book.class)) {
            Preconditions.checkNotNull(title, "title");
            Preconditions.checkNotNull(author, "author");
            Preconditions.checkNotNull(abstract_, "abstract_");
            Preconditions.checkNotNull(publishedYear, "publishedYear");
            Preconditions.checkNotNull(authorId, "authorId");
            Preconditions.checkNotNull(isbn, "isbn");
            Preconditions.checkNotNull(itemId, "itemId");
            Preconditions.checkNotNull(language, "language");
        }
        this.title = title;
        this.author = author;
        this.abstract_ = abstract_;
        this.publishedYear = publishedYear;
        this.authorId = authorId;
        this.isbn = isbn;
        this.itemId = itemId;
        this.language = language;
    }

    public @Nonnull String title() {
        return title;
    }

    public @Nonnull PersonName author() {
        return author;
    }

    public @Nonnull Optional<Abstract> abstract_() {
        return abstract_;
    }

    public @Nonnull Year publishedYear() {
        return publishedYear;
    }

    public @Nonnull Id authorId() {
        return authorId;
    }

    public @Nonnull ISBN isbn() {
        return isbn;
    }

    public @Nonnull ItemId itemId() {
        return itemId;
    }

    public @Nonnull Language language() {
        return language;
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("title", (Object) title)
                .put("author", (Object) author)
                .put("abstract", (Object) abstract_)
                .put("publishedYear", (Object) publishedYear)
                .put("authorId", (Object) authorId)
                .put("isbn", (Object) isbn)
                .put("itemId", (Object) itemId)
                .put("language", (Object) language)
                .build();
    }

    public @Nonnull Book withTitle(@Nonnull String title) {
        return new Book(title, author, abstract_, publishedYear, authorId, isbn, itemId, language);
    }

    public @Nonnull Book withAuthor(@Nonnull PersonName author) {
        return new Book(title, author, abstract_, publishedYear, authorId, isbn, itemId, language);
    }

    public @Nonnull Book withAbstract_(@Nonnull Optional<Abstract> abstract_) {
        return new Book(title, author, abstract_, publishedYear, authorId, isbn, itemId, language);
    }

    public @Nonnull Book withAbstract_(@Nonnull Abstract abstract_) {
        return new Book(title, author, Optional.of(abstract_), publishedYear, authorId, isbn, itemId, language);
    }

    public @Nonnull Book withPublishedYear(@Nonnull Year publishedYear) {
        return new Book(title, author, abstract_, publishedYear, authorId, isbn, itemId, language);
    }

    public @Nonnull Book withAuthorId(@Nonnull Id authorId) {
        return new Book(title, author, abstract_, publishedYear, authorId, isbn, itemId, language);
    }

    public @Nonnull Book withIsbn(@Nonnull ISBN isbn) {
        return new Book(title, author, abstract_, publishedYear, authorId, isbn, itemId, language);
    }

    public @Nonnull Book withItemId(@Nonnull ItemId itemId) {
        return new Book(title, author, abstract_, publishedYear, authorId, isbn, itemId, language);
    }

    public @Nonnull Book withLanguage(@Nonnull Language language) {
        return new Book(title, author, abstract_, publishedYear, authorId, isbn, itemId, language);
    }

    public static @Nonnull Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String title;
        private PersonName author;
        private Year publishedYear;
        private Id authorId;
        private ISBN isbn;
        private ItemId itemId;
        private Language language;
        private Optional<Abstract> abstract_ = Optional.empty();

        Builder() {
        }

        public @Nonnull BuilderWithTitle title(@Nonnull String title) {
            this.title = title;
            return new BuilderWithTitle(this);
        }
    }

    public static final class BuilderWithTitle {

        private final Builder b;

        BuilderWithTitle(Builder b) {
            this.b = b;
        }

        public @Nonnull BuilderWithAuthor author(@Nonnull PersonName author) {
            this.b.author = author;
            return new BuilderWithAuthor(this.b);
        }
    }

    public static final class BuilderWithAuthor {

        private final Builder b;

        BuilderWithAuthor(Builder b) {
            this.b = b;
        }

        public @Nonnull BuilderWithPublishedYear publishedYear(@Nonnull Year publishedYear) {
            this.b.publishedYear = publishedYear;
            return new BuilderWithPublishedYear(this.b);
        }
    }

    public static final class BuilderWithPublishedYear {

        private final Builder b;

        BuilderWithPublishedYear(Builder b) {
            this.b = b;
        }

        public @Nonnull BuilderWithAuthorId authorId(@Nonnull Id authorId) {
            this.b.authorId = authorId;
            return new BuilderWithAuthorId(this.b);
        }
    }

    public static final class BuilderWithAuthorId {

        private final Builder b;

        BuilderWithAuthorId(Builder b) {
            this.b = b;
        }

        public @Nonnull BuilderWithIsbn isbn(@Nonnull ISBN isbn) {
            this.b.isbn = isbn;
            return new BuilderWithIsbn(this.b);
        }
    }

    public static final class BuilderWithIsbn {

        private final Builder b;

        BuilderWithIsbn(Builder b) {
            this.b = b;
        }

        public @Nonnull BuilderWithItemId itemId(@Nonnull ItemId itemId) {
            this.b.itemId = itemId;
            return new BuilderWithItemId(this.b);
        }
    }

    public static final class BuilderWithItemId {

        private final Builder b;

        BuilderWithItemId(Builder b) {
            this.b = b;
        }

        public @Nonnull BuilderWithLanguage language(@Nonnull Language language) {
            this.b.language = language;
            return new BuilderWithLanguage(this.b);
        }
    }

    public static final class BuilderWithLanguage {

        private final Builder b;

        BuilderWithLanguage(Builder b) {
            this.b = b;
        }

        public @Nonnull BuilderWithLanguage abstract_(@Nonnull Abstract abstract_) {
            this.b.abstract_ = Optional.of(abstract_);
            return this;
        }

        public @Nonnull BuilderWithLanguage abstract_(@Nonnull Optional<Abstract> abstract_) {
            this.b.abstract_ = abstract_;
            return this;
        }

        public @Nonnull Book build() {
            return new Book(this.b.title, this.b.author, this.b.abstract_, this.b.publishedYear, this.b.authorId, this.b.isbn, this.b.itemId, this.b.language);
        }
    }

    public static @Nonnull BuilderWithTitle title(@Nonnull String title) {
        return builder().title(title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Book other = (Book) o;
        return 
            Objects.deepEquals(this.title, other.title) && 
            Objects.deepEquals(this.author, other.author) && 
            Objects.deepEquals(this.abstract_, other.abstract_) && 
            Objects.deepEquals(this.publishedYear, other.publishedYear) && 
            Objects.deepEquals(this.authorId, other.authorId) && 
            Objects.deepEquals(this.isbn, other.isbn) && 
            Objects.deepEquals(this.itemId, other.itemId) && 
            Objects.deepEquals(this.language, other.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                title, 
                author, 
                abstract_, 
                publishedYear, 
                authorId, 
                isbn, 
                itemId, 
                language);
    }

    @Override
    public @Nonnull String toString() {
        return Util.toString(Book.class,
                "title", title,
                "author", author,
                "abstract_", abstract_,
                "publishedYear", publishedYear,
                "authorId", authorId,
                "isbn", isbn,
                "itemId", itemId,
                "language", language);
    }
}
