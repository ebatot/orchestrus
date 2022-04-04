
var selected;
var showFocus = function () {
    selected.firstElementChild.style.color = "SlateBlue";
}
var hideFocus = function () {
    selected.firstElementChild.style.color = "";
}
var clickSelected = function () {
    selected.firstElementChild.click();
}
var gotoPrevSibling = function () {
    hideFocus();
    if (selected.previousElementSibling) { selected = selected.previousElementSibling; }
    else if (selected.parentElement.previousElementSibling) {
        liElems = selected.parentElement.previousElementSibling.getElementsByTagName("li");
        if (liElems.length) { selected = liElems[0]; }
    }
    showFocus();
};
var gotoNextSibling = function () {
    hideFocus();
    if (selected.nextElementSibling) { selected = selected.nextElementSibling; }
    else if (selected.parentElement.nextElementSibling) {
        liElems = selected.parentElement.nextElementSibling.getElementsByTagName("li");
        if (liElems.length) { selected = liElems[0]; }
    }
    showFocus();
}
var gotoFirstChild = function () {
    hideFocus();
    firstSub = selected.getElementsByTagName("ul")[0];
    if (firstSub) {
        selected = firstSub.getElementsByTagName("li")[0];
    }
    showFocus();
}
var gotoParent = function () {
    hideFocus();
    if (selected.parentElement && selected.parentElement.parentElement && selected.parentElement.parentElement.id != "tree") {
        selected = selected.parentElement.parentElement;
    }
    showFocus();
}
//var onLoad = function() {
    
    
    var dataPath = "data/GlossaryML/clusters/clustering.tracea.json"
    //if ( getUrlVars()['imf'] != null )
    //    dataPath = getUrlVars()['imf']; 
    
    
    
    var jsonContent = (function () {
        var json = null;
        $.ajax({
            'async': false,
            'global': false,
            'url': dataPath,
            'dataType': "json",
            'success': function (data) {
                json = data;
            }
        });
        return json;
    })(); 

    var projectName = jsonContent.setup.projectName;
    //document.getElementById('title').innerHTML = "Clusters (" + projectName + ")";
    //document.getElementById('projectname').innerHTML =  projectName ;
    
    // 2. render tree structure -- you may make number of sections dynamic insteaf of fixed
    clustertree = document.getElementById('clustertree');
    var json = JSON.parse(JSON.stringify(jsonContent));
    clustertree.innerHTML = renderClusterList(json);
 
    // 3. selection box, default on the first item on the tree
    selected = document.getElementById('clustertree').firstElementChild.getElementsByTagName("li")[0];
    showFocus();

    // 4. click handlers
    var treeListItems = document.querySelectorAll('ul.clustertree a');
    for (var i = 0; i < treeListItems.length; i++) {
        // click handler
        treeListItems[i].addEventListener('click', function(e) {
            var parent = e.target.parentElement;
            hideFocus();
            selected = parent;
            showFocus();
            var classList = parent.classList;
            if (classList.contains("open")) { // close the element and its children
                classList.remove('open');
                var openChildrenList = parent.querySelectorAll(':scope .open');
                for (var j = 0; j < openChildrenList.length; j++) {
                    openChildrenList[j].classList.remove('open');
                }
            } else { // open the element
                classList.add('open');
            }
        });
    }

    // 5. keyboard handler
    document.addEventListener('keydown', function(e) {
            switch (e.key) {
                case "Enter": case " ":
                    actExpandCollapse();
                    break;
                case "ArrowDown":
                    gotoNextSibling();
                    break;
                case "ArrowUp":
                    gotoPrevSibling();
                    break;
                case "ArrowRight":
                    if (selected.classList.contains("open") === false) { clickSelected(); }
                    gotoFirstChild();
                    break;
                case "ArrowLeft":
                    if (selected.classList.contains("open") === true) { clickSelected(); }
                    gotoParent();
                    break;
            }
        });


// };

var expandAll = function() {
    var allListItems = document.querySelectorAll('ul.clustertree li');
    for (var k = 0; k < allListItems.length; k++) {
        allListItems[k].classList.add("open");
    }
};
var collapseAll = function() {
    window.location.reload();
};
var EXPAND_ALL = "Expand All";
var COLLAPSE_ALL = "Collapse All";
var actExpandCollapse = function() {
    var botton = document.getElementById("expand");
    if (botton.innerHTML === EXPAND_ALL) {
        expandAll();
        botton.innerHTML = COLLAPSE_ALL;
    } else {
        collapseAll();
        botton.innerHTML = EXPAND_ALL;
    }
};

function renderClusterList(json) {
    var keys = [];
    var objIsArray = Array.isArray(json); // check if [..] array. Otherwise, it is an ordinary {..} object
    jsonText = '<ul> GlossaryML';
    jsonText += "<li> <a href='index.html'>Trace links</a><li>"
    jsonText +=  "<li><a href='index.html?imf=data/input_data.json'>Fragmentation</a></li>"
    jsonText +=  "</ul>"
    for (var algorithm in json) {
        // KSpan, Label, Newman...
        if(algorithm === 'setup')
            continue;
        jsonText += '<ul>' + algorithm//.setup.algorithm;
        for(var cluster in json[algorithm].clusters) {
            clusterName = json[algorithm].clusters[cluster].name;

/*"data/"+projectName+"/clusters/"+clusterName+".tracea.d3.json"
	//window.open("index.html?imf="+urlClusterD3, '_blank'); //.focus()*/

            var target = "index.html?imf=data/"+projectName+"/clusters/"+clusterName+".tracea.d3.json"

            jsonText += "<li><a href='"+target+"' onClick='loadCluster(\""+clusterName+"\", \""+projectName+"\", \""+algorithm+"\")'>"+clusterName+'</a></li>'
        }
        jsonText += '</ul>'
    }
    return jsonText;
}

function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}
