$(document).ready(function(){
    $('#loader').css('opacity', '0');
    checkURL();

    $('ul li a').click(function (e){
            checkURL(this.hash);
    });

    setInterval("checkURL()",250);

});

var lasturl="";
var d = new Date();
var transition = false;

function checkURL(hash){
    if(!hash) hash=window.location.hash;

    if(hash != lasturl){
        lasturl=hash;
        
        if (transition == false){
            transition = true;
            $('#loader').css('opacity', '1');
            $('#content').toggleClass("loading");
            changeClass($('#'+hash.replace('#page', '')));
            setTimeout(function(){loadPage(hash);}, 200);
        }
    }
}

function loadPage(url){
    url=url.replace('#page','');
    $.ajax({
        type: "POST",
        url: "loadPage.php",
        data: 'page='+url,
        dataType: "html",
        success: function(msg){
            if(parseInt(msg)!=0){
                $('#content').html(msg);
                $('#loader').css('opacity', '0');
                if (transition == true){
                    $('#content').toggleClass("loading");
                    transition = false;
                }
            }
        },
        error: function(msg){
            $('#content').html("Failed to load page: " + msg);
        }
    });
}
