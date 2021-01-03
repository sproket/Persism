/* SAMPLE
 var req = newXMLHttpRequest();
 req.onreadystatechange = getReadyStateHandler(req, displayMenu);
 req.open("GET", "menuFrame.html", false);
 req.send();

 function displayMenu(response) {
    document.getElementById("menu").innerHTML = response.responseText;
 }
*/


/*
 * From http://www-128.ibm.com/developerworks/java/library/j-ajax1/index.html
 * Returns a new XMLHttpRequest object, or false if this browser
 * doesn't support it
 */
function newXMLHttpRequest() {

    var xmlreq = false;

    if (window.XMLHttpRequest) {

        // Create XMLHttpRequest object in non-Microsoft browsers
        xmlreq = new XMLHttpRequest();

    } else if (window.ActiveXObject) {

        // Create XMLHttpRequest via MS ActiveX
        try {
            // Try to create XMLHttpRequest in later versions
            // of Internet Explorer

            xmlreq = new ActiveXObject("Msxml2.XMLHTTP");

        } catch (e1) {

            // Failed to create required ActiveXObject

            try {
                // Try version supported by older versions
                // of Internet Explorer

                xmlreq = new ActiveXObject("Microsoft.XMLHTTP");

            } catch (e2) {

                // Unable to create an XMLHttpRequest with ActiveX
            }
        }
    }

    return xmlreq;
}

/*
 * Returns a function that waits for the specified XMLHttpRequest
 * to complete, then passes its XML response
 * to the given handler function.
 * req - The XMLHttpRequest whose state is changing
 * responseXmlHandler - Function to pass the XML response to
 */
function getReadyStateHandler(req, responseHandler) {

    // Return an anonymous function that listens to the
    // XMLHttpRequest instance
    return function () {

        // If the request's status is "complete"
        if (req.readyState == 4) {

            // Check that a successful server response was received
            if (req.status == 200) {

                // Pass the XML payload of the response to the
                // handler function
                responseHandler(req);

            } else {

                // An HTTP problem has occurred
                alert("HTTP error: " + req.status);
            }
        }
    }
}


/**
 Function getFormValues
 Parameter form object ref
 Author DanH June 27, 2006
 Returns a postable string based on the current form attributes.
 Note that this does post BUTTON becasue we don't know which
 button was clicked.
 */
function getFormValues(fobj) {

    var str = "";

    for (var i = 0; i < fobj.elements.length; i++) {
        //alert(i + " " + fobj.elements[i].name + " " + fobj.elements[i].type);

        if (fobj.elements[i].name.trim().length > 0) {

            switch (fobj.elements[i].type) {
                case "text":

                    str += fobj.elements[i].name +
                           "=" + encodeURIComponent(fobj.elements[i].value) + "&";
                    break;

                case "textarea":

                    str += fobj.elements[i].name +
                           "=" + encodeURIComponent(fobj.elements[i].value) + "&";
                    break;

                case "hidden":

                    str += fobj.elements[i].name +
                           "=" + encodeURIComponent(fobj.elements[i].value) + "&";
                    break;

                case "submit":

                    str += fobj.elements[i].name +
                           "=" + encodeURIComponent(fobj.elements[i].value) + "&";
                    break;

                case "password":

                    str += fobj.elements[i].name +
                           "=" + encodeURIComponent(fobj.elements[i].value) + "&";
                    break;


                case "select-one":

                    var a = getSelectValues(fobj.elements[i]);
                    if (a.length > 0) {
                        str += fobj.elements[i].name +
                               "=" + a[0] + "&";
                    }
                    break;

                case "select-multiple":
                    var a = getSelectValues(fobj.elements[i]);
                    str += fobj.elements[i].name + "=";
                    for (var j = 0; j < a.length; j++) {
                        str += encodeURIComponent(a[j]);
                        if (j < a.length) {
                            str += ",";
                        }
                    }
                    str += "&";
                    break;

                case "checkbox":

                    if (fobj.elements[i].checked) {
                        str += fobj.elements[i].name + "=" + encodeURIComponent(fobj.elements[i].value) + "&";
                    }
                    break;

                case "radio":

                    if (fobj.elements[i].checked) {
                        str += fobj.elements[i].name + "=" + encodeURIComponent(fobj.elements[i].value) + "&";
                    }
                    break;
            }
        }

    }

    str = str.substr(0, (str.length - 1));
    //debugger;
    //alert(str);
    return str;

}

function postForm(formObj, action, callback, assinc) {

    if (typeof assinc == "undefined") {
        assinc = true;
    }

    var sub = getFormValues(formObj);
    var req = newXMLHttpRequest();
    var handlerFunction = getReadyStateHandler(req, callback);
    
    req.onreadystatechange = handlerFunction;
    req.open("POST", action, assinc);
    req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    req.send(sub);

}