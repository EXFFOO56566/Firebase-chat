$(function () {
    // Change the menu nav
    var url = baseUrl + "/histories/add"; // Change the url base on page
    if(typePage == 'edit'){
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
