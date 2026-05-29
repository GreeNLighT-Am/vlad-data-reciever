
package ru.vlad.vlad_data_receiver.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="Document" type="{http://ru.vlad/documents}document" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="TimeStamp" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="blockNum" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="totalDocs" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="dataSet" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="odDocType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "document"
})
@XmlRootElement(name = "Document_Input_Request")
public class DocumentInputRequest
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "Document", required = true)
    protected List<Document> document;
    @XmlAttribute(name = "ID", required = true)
    protected String id;
    @XmlAttribute(name = "TimeStamp", required = true)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected LocalDateTime timeStamp;
    @XmlAttribute(name = "blockNum", required = true)
    protected int blockNum;
    @XmlAttribute(name = "totalDocs", required = true)
    protected int totalDocs;
    @XmlAttribute(name = "dataSet", required = true)
    protected String dataSet;
    @XmlAttribute(name = "odDocType", required = true)
    protected String odDocType;

    /**
     * Gets the value of the document property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the document property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDocument().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Document }
     * 
     * 
     */
    public List<Document> getDocument() {
        if (document == null) {
            document = new ArrayList<Document>();
        }
        return this.document;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setID(String value) {
        this.id = value;
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

    /**
     * Gets the value of the blockNum property.
     * 
     */
    public int getBlockNum() {
        return blockNum;
    }

    /**
     * Sets the value of the blockNum property.
     * 
     */
    public void setBlockNum(int value) {
        this.blockNum = value;
    }

    /**
     * Gets the value of the totalDocs property.
     * 
     */
    public int getTotalDocs() {
        return totalDocs;
    }

    /**
     * Sets the value of the totalDocs property.
     * 
     */
    public void setTotalDocs(int value) {
        this.totalDocs = value;
    }

    /**
     * Gets the value of the dataSet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataSet() {
        return dataSet;
    }

    /**
     * Sets the value of the dataSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataSet(String value) {
        this.dataSet = value;
    }

    /**
     * Gets the value of the odDocType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOdDocType() {
        return odDocType;
    }

    /**
     * Sets the value of the odDocType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOdDocType(String value) {
        this.odDocType = value;
    }

}
