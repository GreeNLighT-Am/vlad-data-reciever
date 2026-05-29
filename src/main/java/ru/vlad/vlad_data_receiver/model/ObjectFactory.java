
package ru.vlad.vlad_data_receiver.model;

import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ru.vlad.vlad_data_receiver.model package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ru.vlad.vlad_data_receiver.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DocumentInputRequest }
     * 
     */
    public DocumentInputRequest createDocumentInputRequest() {
        return new DocumentInputRequest();
    }

    /**
     * Create an instance of {@link Document }
     * 
     */
    public Document createDocument() {
        return new Document();
    }

    /**
     * Create an instance of {@link MOData }
     * 
     */
    public MOData createMOData() {
        return new MOData();
    }

    /**
     * Create an instance of {@link Entries }
     * 
     */
    public Entries createEntries() {
        return new Entries();
    }

    /**
     * Create an instance of {@link SourceSystemHistory }
     * 
     */
    public SourceSystemHistory createSourceSystemHistory() {
        return new SourceSystemHistory();
    }

    /**
     * Create an instance of {@link ParentDocs }
     * 
     */
    public ParentDocs createParentDocs() {
        return new ParentDocs();
    }

    /**
     * Create an instance of {@link DocumentCard }
     * 
     */
    public DocumentCard createDocumentCard() {
        return new DocumentCard();
    }

    /**
     * Create an instance of {@link DocumentAttribute }
     * 
     */
    public DocumentAttribute createDocumentAttribute() {
        return new DocumentAttribute();
    }

    /**
     * Create an instance of {@link DocumentBody }
     * 
     */
    public DocumentBody createDocumentBody() {
        return new DocumentBody();
    }

    /**
     * Create an instance of {@link Content }
     * 
     */
    public Content createContent() {
        return new Content();
    }

    /**
     * Create an instance of {@link AccountingEntry }
     * 
     */
    public AccountingEntry createAccountingEntry() {
        return new AccountingEntry();
    }

    /**
     * Create an instance of {@link Entry }
     * 
     */
    public Entry createEntry() {
        return new Entry();
    }

    /**
     * Create an instance of {@link EntrySum }
     * 
     */
    public EntrySum createEntrySum() {
        return new EntrySum();
    }

    /**
     * Create an instance of {@link Action }
     * 
     */
    public Action createAction() {
        return new Action();
    }

    /**
     * Create an instance of {@link DocumentSigns }
     * 
     */
    public DocumentSigns createDocumentSigns() {
        return new DocumentSigns();
    }

    /**
     * Create an instance of {@link Sign }
     * 
     */
    public Sign createSign() {
        return new Sign();
    }

    /**
     * Create an instance of {@link SignBody }
     * 
     */
    public SignBody createSignBody() {
        return new SignBody();
    }

}
