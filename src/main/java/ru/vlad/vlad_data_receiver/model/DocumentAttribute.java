
package ru.vlad.vlad_data_receiver.model;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import ru.vlad.vlad_data_receiver.adapter.ObjectAdapter;


/**
 * <p>Java class for documentAttribute complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="documentAttribute"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="IfPrimaryKey" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="AttributeCode" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="AttributeValue" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "documentAttribute")
public class DocumentAttribute
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlAttribute(name = "IfPrimaryKey", required = true)
    protected boolean ifPrimaryKey;
    @XmlAttribute(name = "AttributeCode", required = true)
    protected String attributeCode;
    @XmlAttribute(name = "AttributeValue")
    @XmlJavaTypeAdapter(ObjectAdapter.class)
    @XmlSchemaType(name = "anySimpleType")
    protected Object attributeValue;

    /**
     * Gets the value of the ifPrimaryKey property.
     * 
     */
    public boolean isIfPrimaryKey() {
        return ifPrimaryKey;
    }

    /**
     * Sets the value of the ifPrimaryKey property.
     * 
     */
    public void setIfPrimaryKey(boolean value) {
        this.ifPrimaryKey = value;
    }

    /**
     * Gets the value of the attributeCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttributeCode() {
        return attributeCode;
    }

    /**
     * Sets the value of the attributeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttributeCode(String value) {
        this.attributeCode = value;
    }

    /**
     * Gets the value of the attributeValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Object getAttributeValue() {
        return attributeValue;
    }

    /**
     * Sets the value of the attributeValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttributeValue(Object value) {
        this.attributeValue = value;
    }

}
