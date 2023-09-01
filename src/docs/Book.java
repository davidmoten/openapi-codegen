package org.davidmoten.oa3.codegen.test.library.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Generated;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.library.Globals;
import org.davidmoten.oa3.codegen.util.Util;
import org.springframework.boot.context.properties.ConstructorBinding;

@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.4")
public final class Book {

    @JsonProperty("title")
    private final String title;

    @JsonProperty("author")
    private final PersonName author;

    @JsonProperty("abstract")
    private final Abstract abstract_;

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
    private Book(
            @JsonProperty("title") String title,
            @JsonProperty("author") PersonName author,
            @JsonProperty("abstract") Abstract abstract_,
            @JsonProperty("publishedYear") Year publishedYear,
            @JsonProperty("authorId") Id authorId,
            @JsonProperty("isbn") ISBN isbn,
            @JsonProperty("itemId") ItemId itemId,
            @JsonProperty("language") Language language) {
        this.title = title;
        this.author = author;
        this.abstract_ = abstract_;
        this.publishedYear = publishedYear;
        this.authorId = authorId;
        this.isbn = isbn;
        this.itemId = itemId;
        this.language = language;
    }

    @ConstructorBinding
    public Book(
            String title,
            PersonName author,
            Optional<Abstract> abstract_,
            Year publishedYear,
            Id authorId,
            ISBN isbn,
            ItemId itemId,
            Language language) {
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
        this.abstract_ = abstract_.orElse(null);
        this.publishedYear = publishedYear;
        this.authorId = authorId;
        this.isbn = isbn;
        this.itemId = itemId;
        this.language = language;
    }

    public static Builder builder() {
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

        public BuilderWithTitle title(String title) {
            this.title = title;
            return new BuilderWithTitle(this);
        }
    }

    public static final class BuilderWithTitle {

        private final Builder b;

        BuilderWithTitle(Builder b) {
            this.b = b;
        }

        public BuilderWithAuthor author(PersonName author) {
            this.b.author = author;
            return new BuilderWithAuthor(this.b);
        }
    }

    public static final class BuilderWithAuthor {

        private final Builder b;

        BuilderWithAuthor(Builder b) {
            this.b = b;
        }

        public BuilderWithPublishedYear publishedYear(Year publishedYear) {
            this.b.publishedYear = publishedYear;
            return new BuilderWithPublishedYear(this.b);
        }
    }

    public static final class BuilderWithPublishedYear {

        private final Builder b;

        BuilderWithPublishedYear(Builder b) {
            this.b = b;
        }

        public BuilderWithAuthorId authorId(Id authorId) {
            this.b.authorId = authorId;
            return new BuilderWithAuthorId(this.b);
        }
    }

    public static final class BuilderWithAuthorId {

        private final Builder b;

        BuilderWithAuthorId(Builder b) {
            this.b = b;
        }

        public BuilderWithIsbn isbn(ISBN isbn) {
            this.b.isbn = isbn;
            return new BuilderWithIsbn(this.b);
        }
    }

    public static final class BuilderWithIsbn {

        private final Builder b;

        BuilderWithIsbn(Builder b) {
            this.b = b;
        }

        public BuilderWithItemId itemId(ItemId itemId) {
            this.b.itemId = itemId;
            return new BuilderWithItemId(this.b);
        }
    }

    public static final class BuilderWithItemId {

        private final Builder b;

        BuilderWithItemId(Builder b) {
            this.b = b;
        }

        public BuilderWithLanguage language(Language language) {
            this.b.language = language;
            return new BuilderWithLanguage(this.b);
        }
    }

    public static final class BuilderWithLanguage {

        private final Builder b;

        BuilderWithLanguage(Builder b) {
            this.b = b;
        }

        public BuilderWithLanguage abstract_(Abstract abstract_) {
            this.b.abstract_ = Optional.of(abstract_);
            return this;
        }

        public BuilderWithLanguage abstract_(Optional<Abstract> abstract_) {
            this.b.abstract_ = abstract_;
            return this;
        }

        public Book build() {
            return new Book(this.b.title, this.b.author, this.b.abstract_, this.b.publishedYear, this.b.authorId, this.b.isbn, this.b.itemId, this.b.language);
        }
    }

    public static BuilderWithTitle title(String title) {
        return builder().title(title);
    }

    public String title() {
        return title;
    }

    public PersonName author() {
        return author;
    }

    public Optional<Abstract> abstract_() {
        return Optional.ofNullable(abstract_);
    }

    public Year publishedYear() {
        return publishedYear;
    }

    public Id authorId() {
        return authorId;
    }

    public ISBN isbn() {
        return isbn;
    }

    public ItemId itemId() {
        return itemId;
    }

    public Language language() {
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

    public Book withTitle(String title) {
        return new Book(title, author, Optional.ofNullable(abstract_), publishedYear, authorId, isbn, itemId, language);
    }

    public Book withAuthor(PersonName author) {
        return new Book(title, author, Optional.ofNullable(abstract_), publishedYear, authorId, isbn, itemId, language);
    }

    public Book withAbstract_(Optional<Abstract> abstract_) {
        return new Book(title, author, abstract_, publishedYear, authorId, isbn, itemId, language);
    }

    public Book withAbstract_(Abstract abstract_) {
        return new Book(title, author, Optional.of(abstract_), publishedYear, authorId, isbn, itemId, language);
    }

    public Book withPublishedYear(Year publishedYear) {
        return new Book(title, author, Optional.ofNullable(abstract_), publishedYear, authorId, isbn, itemId, language);
    }

    public Book withAuthorId(Id authorId) {
        return new Book(title, author, Optional.ofNullable(abstract_), publishedYear, authorId, isbn, itemId, language);
    }

    public Book withIsbn(ISBN isbn) {
        return new Book(title, author, Optional.ofNullable(abstract_), publishedYear, authorId, isbn, itemId, language);
    }

    public Book withItemId(ItemId itemId) {
        return new Book(title, author, Optional.ofNullable(abstract_), publishedYear, authorId, isbn, itemId, language);
    }

    public Book withLanguage(Language language) {
        return new Book(title, author, Optional.ofNullable(abstract_), publishedYear, authorId, isbn, itemId, language);
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
            Objects.equals(this.title, other.title) && 
            Objects.equals(this.author, other.author) && 
            Objects.equals(this.abstract_, other.abstract_) && 
            Objects.equals(this.publishedYear, other.publishedYear) && 
            Objects.equals(this.authorId, other.authorId) && 
            Objects.equals(this.isbn, other.isbn) && 
            Objects.equals(this.itemId, other.itemId) && 
            Objects.equals(this.language, other.language);
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
    public String toString() {
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
