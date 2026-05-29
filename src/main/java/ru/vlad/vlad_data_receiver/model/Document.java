
package ru.vlad.vlad_data_receiver.model;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for document complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="document"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="DocumentCard" type="{http://ru.vlad/documents}documentCard"/&gt;
 *         &lt;element name="DocumentBody" type="{http://ru.vlad/documents}documentBody"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="SeqN" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "document", propOrder = {
    "documentCard",
    "documentBody"
})
public class Document
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "DocumentCard", required = true)
    protected DocumentCard documentCard;
    @XmlElement(name = "DocumentBody", required = true)
    protected DocumentBody documentBody;
    @XmlAttribute(name = "SeqN", required = true)
    protected int seqN;

    /**
     * Gets the value of the documentCard property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentCard }
     *     
     */
    public DocumentCard getDocumentCard() {
        return documentCard;
    }

    /**
     * Sets the value of the documentCard property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentCard }
     *     
     */
    public void setDocumentCard(DocumentCard value) {
        this.documentCard = value;
    }

    /**
     * Gets the value of the documentBody property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentBody }
     *     
     */
    public DocumentBody getDocumentBody() {
        return documentBody;
    }

    /**
     * Sets the value of the documentBody property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentBody }
     *     
     */
    public void setDocumentBody(DocumentBody value) {
        this.documentBody = value;
    }

    /**
     * Gets the value of the seqN property.
     * 
     */
    public int getSeqN() {
        return seqN;
    }

    /**
     * Sets the value of the seqN property.
     * 
     */
    public void setSeqN(int value) {
        this.seqN = value;
    }

}
