//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.05.16 at 07:42:17 PM CEST 
//


package URTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Contains all information concerning a Ecall request
 *       
 * 
 * <p>Java class for EcallRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EcallRequest">
 *   &lt;complexContent>
 *     &lt;extension base="{http://not.ima.tm.fr/telematic}ServiceRequest">
 *       &lt;sequence>
 *         &lt;element name="automatic" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="callType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="requestObject" type="{http://not.ima.tm.fr/telematic}RequestObject"/>
 *         &lt;element name="contextData" type="{http://not.ima.tm.fr/telematic}ContextObject" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EcallRequest", propOrder = {
    "automatic",
    "callType",
    "requestObject",
    "contextData"
})
public class EcallRequest
    extends ServiceRequest
{

    protected boolean automatic;
    @XmlElement(required = true)
    protected String callType;
    @XmlElement(required = true)
    protected RequestObject requestObject;
    protected ContextObject contextData;

    /**
     * Gets the value of the automatic property.
     * 
     */
    public boolean isAutomatic() {
        return automatic;
    }

    /**
     * Sets the value of the automatic property.
     * 
     */
    public void setAutomatic(boolean value) {
        this.automatic = value;
    }

    /**
     * Gets the value of the callType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCallType() {
        return callType;
    }

    /**
     * Sets the value of the callType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCallType(String value) {
        this.callType = value;
    }

    /**
     * Gets the value of the requestObject property.
     * 
     * @return
     *     possible object is
     *     {@link RequestObject }
     *     
     */
    public RequestObject getRequestObject() {
        return requestObject;
    }

    /**
     * Sets the value of the requestObject property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequestObject }
     *     
     */
    public void setRequestObject(RequestObject value) {
        this.requestObject = value;
    }

    /**
     * Gets the value of the contextData property.
     * 
     * @return
     *     possible object is
     *     {@link ContextObject }
     *     
     */
    public ContextObject getContextData() {
        return contextData;
    }

    /**
     * Sets the value of the contextData property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContextObject }
     *     
     */
    public void setContextData(ContextObject value) {
        this.contextData = value;
    }

}