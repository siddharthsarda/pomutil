// Copyright Keith D Gregory
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.kdgregory.pomutil.util;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.sf.kdgcommons.lang.StringUtil;
import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.xpath.XPathWrapperFactory;
import net.sf.practicalxml.xpath.XPathWrapperFactory.CacheType;


/**
 *  Holds a parsed POM and provides access to its various sections.
 */
public class PomWrapper
{
    private Document dom;

    private XPathWrapperFactory xpFact = new XPathWrapperFactory(CacheType.SIMPLE)
                                         .bindNamespace("mvn", "http://maven.apache.org/POM/4.0.0");

    public PomWrapper(Document dom)
    {
        this.dom = dom;
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Returns the DOM wrapped by this object.
     */
    public Document getDom()
    {
        return dom;
    }


    /**
     *  Replaces the DOM wrapped by this object.
     */
    public void setDom(Document dom)
    {
        this.dom = dom;
    }


    /**
     *  Executes the passed XPath against the entire POM, and returns its string
     *  value. Path components must be prefixed with "mvn" to use the Maven namespace.
     */
    public String selectValue(String xpath)
    {
        return selectValue(dom, xpath);
    }


    /**
     *  Executes the passed XPath against the specified node, and returns its string
     *  value. Path components must be prefixed with "mvn" to use the Maven namespace.
     */
    public String selectValue(Node node, String xpath)
    {
        return xpFact.newXPath(xpath).evaluateAsString(node);
    }


    /**
     *  Executes the passed XPath against the POM and returns the single element
     *  that it selects, <code>null</code> if it doesn't select anything. Path
     *  components must be prefixed with "mvn" to use the Maven namespace.
     */
    public Element selectElement(String xpath)
    {
        return selectElement(dom, xpath);
    }


    /**
     *  Executes the passed XPath against the specified node and returns the single
     *  element that it selects, <code>null</code> if it doesn't select anything.
     *  Path components must be prefixed with "mvn" to use the Maven namespace.
     */
    public Element selectElement(Node node, String xpath)
    {
        return xpFact.newXPath(xpath).evaluateAsElement(node);
    }


    /**
     *  Executes the passed XPath against the POM and returns the elements that
     *  it selects. Path components must be prefixed with "mvn" to use the Maven
     *  namespace.
     */
    public List<Element> selectElements(String xpath)
    {
        return selectElements(dom, xpath);
    }


    /**
     *  Executes the passed XPath against the specified node and returns the elements
     *  that it selects. Path components must be prefixed with "mvn" to use the Maven
     *  namespace.
     */
    public List<Element> selectElements(Node node, String xpath)
    {
        return xpFact.newXPath(xpath).evaluate(node, Element.class);
    }


    /**
     *  Selects the element specified by the given xpath, if it exists. If it doesn't
     *  exist, selects the parent element (recursively) and appends an element with
     *  the desired name (which is extracted from the path). Steps in the path must
     *  be prefixed with "mvn" to use the Maven namespace.
     *  <p>
     *  To properly create the child element, the last step in the path must be a
     *  simple node selector, of the form "mvn:NAME".
     */
    public Element selectOrCreateElement(String xpath)
    {
        Element elem = selectElement(xpath);
        if (elem != null)
            return elem;

        String parentPath = StringUtil.extractLeftOfLast(xpath, "/");
        Element parent = selectOrCreateElement(parentPath);

        String childPath = StringUtil.extractRightOfLast(xpath, "/");
        if (!childPath.startsWith("mvn:"))
            throw new IllegalArgumentException("last element of path must have \"mvn\" prefix: " + childPath);

        String childName = childPath.substring(4);

        return DomUtil.appendChildInheritNamespace(parent, childName);
    }
    
    
    /**
     *  Returns the group/artifact/version tuple from the passed element (which
     *  may be any type of dependency reference).
     */
    public GAV extractGAV(Element elem)
    {
        return new GAV(selectValue(elem, "mvn:groupId"),
                       selectValue(elem, "mvn:artifactId"),
                       selectValue(elem, "mvn:version"));
    }


    /**
     *  Selects the element identified by the given XPath, and removes all of its
     *  children. Will throw if the path does not select an element.
     */
    public Element clear(String xpath)
    {
        return clear(selectElement(xpath));
    }


    /**
     *  Removes all children from the passed element. Note that you can hold another
     *  reference to the child and re-attach it to the same or different element.
     */
    public Element clear(Element elem)
    {
        Node child = elem.getFirstChild();
        while (child != null)
        {
            Node nextChild = child.getNextSibling();
            elem.removeChild(child);
            child = nextChild;
        }

        return elem;
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------
}
