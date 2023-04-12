var map;
var drawingManager;
var shapes = [];
var myPolygon;
var drawShapes = [];
var checkLocation;
var htmlStr;

$(function() {
    // Change the menu nav
    var url = baseUrl + "/areas/add"; // Change the url base on page
    if (typePage == 'edit') {
        $('ul.nav-sidebar').find('a.nav-link').filter(function() {
            return this.href == url;
        }).addClass('active');

        $('ul.nav-sidebar').find('a.nav-link').filter(function() {
            return this.href == url;
        }).parent().parent().parent().addClass('menu-open');

        $('ul.nav-sidebar').find('a.nav-link').filter(function() {
            return this.href == url;
        }).parent().parent().parent().find('a.nav-item').addClass('active');
    }
});

/**
 * Init google maps
 *
 */
function initialize() {
    var myLatlng = new google.maps.LatLng(51.51686166794058, 3.5945892333984375);
    var mapOptions = {
        zoom: 12,
        center: myLatlng,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };

    // Getting map DOM element
    var mapElement = document.getElementById('map-canvas');

    map = new google.maps.Map(mapElement, mapOptions);

    drawingManager = new google.maps.drawing.DrawingManager({
        drawingMode: google.maps.drawing.OverlayType.POLYGON,
        drawingControl: true,
        drawingControlOptions: {
            position: google.maps.ControlPosition.TOP_CENTER,
            drawingModes: [google.maps.drawing.OverlayType.POLYGON]
        },
        polygonOptions: {
            editable: true,
            // draggable: true
        }
    });

    drawingManager.setMap(map);

    // Check is there any data location of area
    $.ajax({
        url: baseUrl + "/areas/showAllDataLocation/" + $('#id').val(),
        type: "get",
        success: function(response) {
            if (response.length > 0) {
                for (var i = 0; i < response.length; i++) {
                    drawShapes.push(new google.maps.LatLng(response[i].lat, response[i].longt));
                }
            } else {
                checkLocation = "No Data";
                console.log("No Data");
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            // Error
        }
    }).done(function(response) {
        myPolygon = new google.maps.Polygon({
            path: drawShapes,
            editable: true,
            // draggable: true
        });

        if (checkLocation == "No Data") {
            myLatlng = new google.maps.LatLng(51.51686166794058, 3.5945892333984375);
        } else {
            myLatlng = new google.maps.LatLng(response[0].lat, response[0].longt);
        }
        mapOptions = {
            zoom: 16,
            center: myLatlng,
            mapTypeId: google.maps.MapTypeId.ROADMAP
        }

        map = new google.maps.Map(mapElement, mapOptions);

        myPolygon.setMap(map);
        drawingManager.setDrawingMode(null);
        drawingManager.setMap(map);

        google.maps.event.addListener(myPolygon.getPath(), "insert_at", getPolygonCoordsFirst);
        google.maps.event.addListener(myPolygon.getPath(), "set_at", getPolygonCoordsFirst);
    });

    // Add a listener for creating new shape event.
    google.maps.event.addListener(drawingManager, "overlaycomplete", function(event) {

        var newShape = event.overlay;
        newShape.type = event.type;
        shapes.push(newShape);
        if (drawingManager.getDrawingMode()) {
            drawingManager.setDrawingMode(null);
        }
        getPolygonCoords(event.overlay);
    });

    // add a listener for the drawing mode change event, delete any existing polygons
    google.maps.event.addListener(drawingManager, "drawingmode_changed", function() {
        if (drawingManager.getDrawingMode() != null) {
            myPolygon.setMap(null);
            for (var i = 0; i < shapes.length; i++) {
                shapes[i].setMap(null);
            }
            shapes = [];
        }
    });

    // Add a listener for the "drag" event.
    google.maps.event.addListener(drawingManager, "overlaycomplete", function(event) {
        overlayDragListener(event.overlay);
        $('#vertices').val(event.overlay.getPath().getArray());
    });
}

function overlayDragListener(overlay) {
    google.maps.event.addListener(overlay.getPath(), 'set_at', function(event) {
        $('#vertices').val(overlay.getPath().getArray());
        getPolygonCoords(overlay);
    });
    google.maps.event.addListener(overlay.getPath(), 'insert_at', function(event) {
        $('#vertices').val(overlay.getPath().getArray());
        getPolygonCoords(overlay);
    });
}

// Display Coordinates below map
function getPolygonCoordsFirst() {
    var len = myPolygon.getPath().getLength();
    var checkLast = len - 1;
    htmlStr = "";
    for (var i = 0; i < len; i++) {

        if (i == checkLast) {
            htmlStr += myPolygon.getPath().getAt(i).toUrlValue(10);
        } else {
            htmlStr += myPolygon.getPath().getAt(i).toUrlValue(10) + "---";
        }
    }
    document.getElementById('info').innerHTML = htmlStr;
}

// Display Coordinates below map
function getPolygonCoords(overlay) {
    var len = overlay.getPath().getLength();
    var checkLast = len - 1;
    htmlStr = "";
    for (var i = 0; i < len; i++) {

        if (i == checkLast) {
            htmlStr += overlay.getPath().getAt(i).toUrlValue(10);
        } else {
            htmlStr += overlay.getPath().getAt(i).toUrlValue(10) + "---";
        }
    }
    document.getElementById('info').innerHTML = htmlStr;
}

$('#saveLocation').click(function(event) {
    if (typeof htmlStr !== "undefined") {
        deleteLocationTable();

        setTimeout(function() {
            var getCoordinate = htmlStr.split("---");

            var i = 0;
            var myTimer = setInterval(function() {
                saveLocation(getCoordinate[i], $('#id').val());
                i++;

                if (getCoordinate.length == i) {
                    clearInterval(myTimer);

                    submitAndCheckFields(event);
                }

            }, 900);

        }, 1000);
    } else {
        submitAndCheckFields(event);
    }
});

function submitAndCheckFields(event) {
    if ($('form#areaId')[0].checkValidity()) {
        // Submit the form after all done
        $('form#areaId').submit();
    } else {
        event.preventDefault();
        alert("Name and Address cannot be blank");
        return false;
    }
}

function saveLocation(latLongt, idArea) {
    var splitCoordinate = latLongt.split(','),
        data;

    data = {
        'area_id': idArea,
        'lat': splitCoordinate[0],
        'longt': splitCoordinate[1],
        '_token': document.querySelector('meta[name="csrf-token"]').content,
    };

    $.ajax({
        url: baseUrl + "/areas/storeLocation",
        type: "post",
        data: data,
        success: function(response) {
            // Success
        },
        error: function(jqXHR, textStatus, errorThrown) {
            // Error
        }
    });
}

function deleteLocationTable() {
    $('.reload').css('display', 'block');
    $.ajax({
        url: baseUrl + "/areas/deleteLocationTable",
        type: "post",
        data: {
            '_token': document.querySelector('meta[name="csrf-token"]').content,
            'area_id': $('#id').val()
        },
        success: function(response) {
            // Success
        },
        error: function(jqXHR, textStatus, errorThrown) {
            // Error
        }
    });

    setTimeout(function() {
        $('.reload').css('display', 'none');
    }, 6000);
}


google.maps.event.addDomListener(window, 'load', initialize);