var exec = require('cordova/exec');

/*exports.coolMethod = function (arg0, success, error) {
    console.log("i Am in Plugin js");
    exec(success, error, 'Mapboxplugin', 'coolMethod', [arg0]);
};*/
exports.show = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "show", [arg0]);
  };
  exports.remove = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "remove", [arg0]);
  };
  exports.hide = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "hide", [arg0]);
  };
  
  exports.addMarkers = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "addMarkers", [arg0]);
  };
  exports.removeAllMarkers = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "removeAllMarkers", [arg0]);
  };
  exports.addMarkerCallback = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "addMarkerCallback", [arg0]);
  };
  exports.animateCamera = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "animateCamera", [arg0]);
  };
  exports.addGeoJSON = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "addGeoJSON", [arg0]);
  };
  
  exports.setCenter = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "setCenter", [arg0]);
  };
  
  exports.getCenter = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "getCenter", [arg0]);
  };
  exports.setTilt = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "setTilt", [arg0]);
  };
  exports.getTilt = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "getTilt", [arg0]);
  };
  exports.getZoomLevel = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "getZoomLevel", [arg0]);
  };
  
  exports.setZoomLevel = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "setZoomLevel", [arg0]);
  };
  
  exports.getBounds = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "getBounds", [arg0]);
  };
  
  exports.setBounds = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "setBounds", [arg0]);
  };
  
  exports.addPolygon = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "addPolygon", [arg0]);
  };
  
  exports.convertCoordinate = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "convertCoordinate", [arg0]);
  };
  
  exports.convertPoint = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "convertPoint", [arg0]);
  };
  
  exports.onRegionWillChange = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "onRegionWillChange", [arg0]);
  };
  
  exports.onRegionIsChanging = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "onRegionIsChanging", [arg0]);
  };
  
  exports.onRegionDidChange = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "onRegionDidChange", [arg0]);
  };
  
  exports.addCustomButton = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "addCustomButton", [arg0]);
  };
  exports.editCustomButton = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "editCustomButton", [arg0]);
  };
  exports.removeCustomButton = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "removeCustomButton", [arg0]);
  };
  exports.addCustomButtonCallback = function(arg0, success, error) {
    exec(success, error, "Mapboxplugin", "addCustomButtonCallback", [arg0]);
  };
