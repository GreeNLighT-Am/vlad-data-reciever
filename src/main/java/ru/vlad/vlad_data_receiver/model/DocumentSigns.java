
package ru.vlad.vlad_data_receiver.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for documentSigns complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="documentSigns"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Sign" type="{http://ru.vlad/documents}sign" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "documentSigns", propOrder = {
    "sign"
})
public class DocumentSigns
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "Sign", required = true)
    protected List<Sign> sign;

    /**
     * Gets the value of the sign property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the sign property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSign().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Sign }
     * 
     * 
     */
    public List<Sign> getSign() {
        if (sign == null) {
            sign = new ArrayList<Sign>();
        }
        return this.sign;
    }

}
