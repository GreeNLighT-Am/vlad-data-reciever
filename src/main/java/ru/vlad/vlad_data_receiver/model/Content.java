
package ru.vlad.vlad_data_receiver.model;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for content complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="content"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Data" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/&gt;
 *         &lt;element name="Url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="AccountingEntry" type="{http://ru.vlad/documents}accountingEntry" minOccurs="0"/&gt;
 *         &lt;element name="DocumentSigns" type="{http://ru.vlad/documents}documentSigns" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Format" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "content", propOrder = {
    "data",
    "url",
    "accountingEntry",
    "documentSigns"
})
public class Content
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "Data", required = true)
    protected byte[] data;
    @XmlElement(name = "Url")
    protected String url;
    @XmlElement(name = "AccountingEntry")
    protected AccountingEntry accountingEntry;
    @XmlElement(name = "DocumentSigns")
    protected DocumentSigns documentSigns;
    @XmlAttribute(name = "Format", required = true)
    protected String format;

    /**
     * Gets the value of the data property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setData(byte[] value) {
        this.data = value;
    }

    /**
     * Gets the value of the url property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     * Gets the value of the accountingEntry property.
     * 
     * @return
     *     possible object is
     *     {@link AccountingEntry }
     *     
     */
    public AccountingEntry getAccountingEntry() {
        return accountingEntry;
    }

    /**
     * Sets the value of the accountingEntry property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccountingEntry }
     *     
     */
    public void setAccountingEntry(AccountingEntry value) {
        this.accountingEntry = value;
    }

    /**
     * Gets the value of the documentSigns property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentSigns }
     *     
     */
    public DocumentSigns getDocumentSigns() {
        return documentSigns;
    }

    /**
     * Sets the value of the documentSigns property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentSigns }
     *     
     */
    public void setDocumentSigns(DocumentSigns value) {
        this.documentSigns = value;
    }

    /**
     * Gets the value of the format property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the value of the format property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormat(String value) {
        this.format = value;
    }

}
