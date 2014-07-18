$(document).ready(function(){
    $('#loader').css('opacity', '0');
    checkURL();

    $('#nav ul li a').click(function (e){
            checkURL(this.hash);
    });

    setInterval("checkURL()",250);

});

var lasturl = "";
var hashes = [];
var inUse = [];

function checkURL(hash){
    if (!hash) hash=window.location.hash;
    if (hash != lasturl){
        lasturl = hash;
        hashes[hashes.length] = hash;
        $('#loader').css('opacity', '1');
        $('#content').addClass("loading");
        changeClass($('a[href^=#' + hash + ']'));
        loadPage();
    }
}

function loadPage(){
    inUse[inUse.length]=hashes[0].replace('#page','');
    hashes.splice(0, 1);
    var request = $.ajax({
        type: "GET",
        url: "pages/" + inUse[0] + ".html",
        dataType: "html",
    });
    setTimeout(function(){
        request.done(function(msg){
            $('#content').html(msg);
            $('#loader').css('opacity', '0');
            $('#content').removeClass("loading");
            inUse.splice(0, 1);
        });
        request.fail(function(jqXHR, msg){
            $('#content').html("<h1>Failed to load page!</h1><br>Status: " + msg + ".");
            $('#content').removeClass("loading");
            $('#loader').css('opacity', '0');
            inUse.splice(0, 1);
        });
    }, 250);
}
