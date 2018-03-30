//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.05.16 at 07:42:22 PM CEST 
//


package SOS;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TestimonialRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TestimonialRequest">
 *   &lt;complexContent>
 *     &lt;extension base="{http://not.ima.tm.fr/telematic}ServiceRequest">
 *       &lt;sequence>
 *         &lt;element name="destinationCountry" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="departureTimestamp" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="returnTimestamp" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="recipients" type="{http://not.ima.tm.fr/telematic}Recipient" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestimonialRequest", propOrder = {
    "destinationCountry",
    "departureTimestamp",
    "returnTimestamp",
    "recipients"
})
public class TestimonialRequest
    extends ServiceRequest
{

    @XmlElement(required = true)
    protected String destinationCountry;
    @XmlElement(required = true)
    protected String departureTimestamp;
    @XmlElement(required = true)
    protected String returnTimestamp;
    protected List<Recipient> recipients;

    /**
     * Gets the value of the destinationCountry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestinationCountry() {
        return destinationCountry;
    }

    /**
     * Sets the value of the destinationCountry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestinationCountry(String value) {
        this.destinationCountry = value;
    }

    /**
     * Gets the value of the departureTimestamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDepartureTimestamp() {
        return departureTimestamp;
    }

    /**
     * Sets the value of the departureTimestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDepartureTimestamp(String value) {
        this.departureTimestamp = value;
    }

    /**
     * Gets the value of the returnTimestamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReturnTimestamp() {
        return returnTimestamp;
    }

    /**
     * Sets the value of the returnTimestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReturnTimestamp(String value) {
        this.returnTimestamp = value;
    }

    /**
     * Gets the value of the recipients property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the recipients property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRecipients().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Recipient }
     * 
     * 
     */
    public List<Recipient> getRecipients() {
        if (recipients == null) {
            recipients = new ArrayList<Recipient>();
        }
        return this.recipients;
    }

}