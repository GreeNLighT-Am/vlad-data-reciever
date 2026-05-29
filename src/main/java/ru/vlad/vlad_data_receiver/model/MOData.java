
package ru.vlad.vlad_data_receiver.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import ru.vlad.vlad_data_receiver.adapter.DateTimeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Entries" type="{http://ru.vlad/documents}entries"/&gt;
 *         &lt;element name="SourceSystemHistory" type="{http://ru.vlad/documents}sourceSystemHistory" minOccurs="0"/&gt;
 *         &lt;element name="ParentDocs" type="{http://ru.vlad/documents}parentDocs" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="TimeStamp" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "entries",
    "sourceSystemHistory",
    "parentDocs"
})
@XmlRootElement(name = "MO_Data")
public class MOData
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "Entries", required = true)
    protected Entries entries;
    @XmlElement(name = "SourceSystemHistory")
    protected SourceSystemHistory sourceSystemHistory;
    @XmlElement(name = "ParentDocs")
    protected ParentDocs parentDocs;
    @XmlAttribute(name = "TimeStamp", required = true)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected LocalDateTime timeStamp;

    /**
     * Gets the value of the entries property.
     * 
     * @return
     *     possible object is
     *     {@link Entries }
     *     
     */
    public Entries getEntries() {
        return entries;
    }

    /**
     * Sets the value of the entries property.
     * 
     * @param value
     *     allowed object is
     *     {@link Entries }
     *     
     */
    public void setEntries(Entries value) {
        this.entries = value;
    }

    /**
     * Gets the value of the sourceSystemHistory property.
     * 
     * @return
     *     possible object is
     *     {@link SourceSystemHistory }
     *     
     */
    public SourceSystemHistory getSourceSystemHistory() {
        return sourceSystemHistory;
    }

    /**
     * Sets the value of the sourceSystemHistory property.
     * 
     * @param value
     *     allowed object is
     *     {@link SourceSystemHistory }
     *     
     */
    public void setSourceSystemHistory(SourceSystemHistory value) {
        this.sourceSystemHistory = value;
    }

    /**
     * Gets the value of the parentDocs property.
     * 
     * @return
     *     possible object is
     *     {@link ParentDocs }
     *     
     */
    public ParentDocs getParentDocs() {
        return parentDocs;
    }

    /**
     * Sets the value of the parentDocs property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParentDocs }
     *     
     */
    public void setParentDocs(ParentDocs value) {
        this.parentDocs = value;
    }

    /**
     * Gets the value of the timeStamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the value of the timeStamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeStamp(LocalDateTime value) {
        this.timeStamp = value;
    }

}
