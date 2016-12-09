<html>
  <head>
    <meta charset='utf-8'>
    <title>Valmostrato - Pallone Sonda</title>
    <link rel="shortcut icon" href="img/icon.ico" />
    <meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no'/>
    <style>
      // mettere in pixel la grandezza di logoValmostrato
      // cambiare logo grafico o rimpicciolirlo
      html { height: 100%; }
      body { height: 100%; margin: 0px; padding: 0px; }
      li { font-family: Arial; }
      #map { position: absolute; width: 100%; z-index: 1; height: 100%; }
      #opacityBackground { position: absolute; z-index: 3; width: 100%; height: 100%; background-color: rgba(0, 0, 0, 0.5); display: none; }
      #graphBackground { position: absolute; z-index: 4; left: 6.5%; top: 20%; width: 70%; height: 60%; background-color: rgba(255, 255, 255, 1); display: none; border-radius: 25px; }
      #graph { position: absolute; z-index: 5; display: none; width: 65%; height: 60%; top: 5%; }
      #logoValmostrato { position: fixed; z-index: 2; width: 25%; height: 7%; left: 37.5%; top: 1%; }
      #logoValmostrato:hover { width: 26%; height: 8%; left: 37%; }
      #logoGraph { position: absolute; z-index: 2; width: 40px; height: 40px; top: 370px; left: 25px; }
      #logoGraph:hover { width: 55px; height: 55px; top: 362.5; left: 17.5; }
      #legenda { position: absolute; z-index: 7;  width: 14%; height: 60%; top: 20%; left: 78.5%; background-color: white; display: none; border-radius: 25px; font-size: 100%; padding-left: 10px; padding-right: 10px;}
      #listaLegenda { list-style-type: none;} 
    </style>
    <script src="Chart.js"></script>
    <script src='http://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false'></script>
    <script>
      var markers = Array();
      function initialize() 
      {
        <?php
          $i = 0;
          $j = 0;
          $intervalloValori = 10;
          $fLastPos = fopen('lastpos.txt','r');
          $lastPos  = fgets($fLastPos);
          $exLast   = explode(',', $lastPos);
          fclose($fLastPos);
          if ((floatval($exLast[0]) == 0) && (floatval($exLast[1]) == 0))
          {
            $x = 45.816667;
            $y = 8.933333;
            echo"var fenway = new google.maps.LatLng(" . $x . "," . $y . ");";
          }
          else
          {
            echo "var fenway = new google.maps.LatLng(" . $exLast[0] . ", (" . $exLast[1] . "));";
          }
          echo"
            var mapOptions = {
              center: fenway,
              zoom: 15,
              mapTypeId: google.maps.MapTypeId.ROADMAP
            };
            var map = new google.maps.Map(document.getElementById('map'), mapOptions);";
            $file = fopen('data.txt', "r");
            while(!feof($file))
            {
              $line = fgets($file);
              if ($line != '') 
              {
                $parts = explode(',', $line);
                if ((floatval($parts[0]) != 0) && (floatval($parts[1]) != 0))
                {
                  $j++;
                  if ($j % $intervalloValori == 0)
                  {
                    $i++;
                    $graphString[$i]["'ora'"] = substr($parts[3], 8, 2) . ":" . substr($parts[3], 10, 2) . ":" . substr($parts[3], 12, 2);                 
                    $graphString[$i]["'velocità'"] = round($parts[5] / 3.6 * 1.852, 2);            
                    $graphString[$i]["'altezza'"] = $parts[2];              
                    $graphString[$i]["'temperaturaInterna'"] = $parts[7];   
                    $graphString[$i]["'temperaturaEsterna'"] = $parts[6];   
                    $graphString[$i]["'pressione'"] = $parts[8] / 100;            
                    $graphString[$i]["'tensione'"] = $parts[9];                 
                  }
                  echo"
                  var myContent = '<div>Altezza : " . $parts[2] . " m </br> Velocita : " . round($parts[5] / 3.6 * 1.852, 2) . " km/h </br> Ora : " . substr($parts[3], 8, 2) . ":" . substr($parts[3], 10, 2) . ":" . substr($parts[3], 12, 2) . "</br> Data : "  . substr($parts[3], 6, 2) . "/" . substr($parts[3], 4, 2) . "/" . substr($parts[3], 0, 4) . " </br> Temperatura Int : " . $parts[7]   . " C </br> Temperatura Est : " . $parts[6] . " C </br> N Sat : " . $parts[4] . " </br> Pressione : " . $parts[8] / 100 . " hPa  </br> V. Batt : " . trim(preg_replace('/\s\s+/', ' ', $parts[9])) . "V </br> N Marker : " . $j . "</div>';
                  var contentwindow = myContent;
                  var pos = new google.maps.LatLng(" . $parts[0] . "," . $parts[1] . ");
                  var marker = new google.maps.Marker
                  ({
                    position: pos,
                    map: map,
                    opacity : 0.5,
                  });
                  marker['infowindow'] = new google.maps.InfoWindow
                  ({
                    content: contentwindow
                  });
                  google.maps.event.addListener(marker, 'click', function()
                  {
                    this['infowindow'].open(map, this);
                  });   
                  markers.push(marker);";
                }
              }
            }
            function drawX($graphString)
            {
              echo "labels : ['" . $graphString[1]["'ora'"] . "'";
              for ($a = 2; $a <= sizeof($graphString); $a++)
              {
                echo ",'" . $graphString[$a]["'ora'"] . "'";
              }
              echo "],";
            }
            function insertData($graphString, $label, $color, $conversion, $unit)
            {
              echo "{label: " . $label .  
                   ",strokeColor: " . $color . 
                   ",pointColor: " . $color . 
                   ",pointStrokeColor: '#fff'
                    ,pointHighLightFill: '#fff'
                    ,pointHighlightStroke: " . $color;
              $color[18] = "0";
              echo ", fillColor: " . $color;
              echo ",data : [" . $graphString[1][$label] / $conversion;
              for ($a = 2; $a <= sizeof($graphString); $a++)
              {
                echo "," . $graphString[$a][$label] / $conversion;
              }
              echo "]}";
            }
        ?>
        var flightPath;
        var pathLine = Array();
        google.maps.event.addListener(map, 'click', function()
        {
          if (flightPath == null)
          {
            for(a=0;a < markers.length; a++)
            {
              pathLine[a] = markers[a].position;
            }
            flightPath = new google.maps.Polyline
            ({
              path: pathLine,
              geodesic: true,
              strokeColor: '#FF0000',
              strokeOpacity: 1.0,
              strokeWeight: 2
            });
            flightPath.setMap(map);   
          }
          else
          {
            flightPath.setMap(null);
            flightPath = null;
          }    
        });
        map.setCenter({lat: markers[markers.length-1].position.lat() , lng: markers[markers.length-1].position.lng()});
        markers[markers.length - 1] = new google.maps.Marker({
          position: pos,
          map: map,
          opacity : 1.0,
          title : (markers.length).toString()
        });
        markers[markers.length - 1]['infowindow'] = new google.maps.InfoWindow
        ({
          content: contentwindow
        });
        google.maps.event.addListener(markers[markers.length - 1], 'click', function(event)
        {
          this['infowindow'].open(map, this);
        });
        <?php
          fclose($file);         
        ?>    
      }
      google.maps.event.addDomListener(window, 'load', initialize);
    </script>
    <script>
      function viewGraph()
      {
        document.getElementById("opacityBackground").style.display = "inline";
        document.getElementById("graphBackground").style.display = "inline";
        document.getElementById("graph").style.display = "inline";
        document.getElementById("legenda").style.display = "inline";
        document.getElementById("logoGraph").style.display = "none";
        var ctx = document.getElementById("graph").getContext("2d");
        window.myLine = new Chart(ctx).Line(lineChartData, { responsive: true });
      }
      function hideGraph()
      {
        document.getElementById("opacityBackground").style.display = "none";
        document.getElementById("graphBackground").style.display = "none";
        document.getElementById("graph").style.display = "none";
        document.getElementById("legenda").style.display = "none";
        document.getElementById("logoGraph").style.display = "inline";
      }
      var lineChartData = 
      {
        <?php
          drawX($graphString);
          echo "datasets : [";
          insertData($graphString, "'temperaturaInterna'" , "'rgba(204,000,000,1)'", 1, "°C");
          echo ",";
          insertData($graphString, "'temperaturaEsterna'" , "'rgba(255,051,000,1)'", 1, "°C");
          echo ",";
          insertData($graphString, "'tensione'" , "'rgba(000,204,000,1)'", 1, "V");
          echo ",";
          insertData($graphString, "'pressione'" , "'rgba(000,153,204,1)'", 100, "daPa");
          echo ",";
          insertData($graphString, "'altezza'" , "'rgba(255,153,000,1)'", 100, "hm");
          echo ",";
          insertData($graphString, "'velocità'" , "'rgba(000,051,000,1)'", 1, "km/h");
          echo "]}"
        ?>
    </script>
  </head>
  <body>
    <img id='logoValmostrato' class="imageLink" src='img/logo.PNG' onclick="window.open('http://www.meteovalmorea.it/valmostrato','_new')"/>
    <div id='map'></div>
    <!--<center>-->
      <div id="legenda">
        <ul id="listaLegenda">
          <br /><br /><br /><br /><br /><br /><br />
          <li style="color: rgb(204,000,000)">Temperatura<br />Interna (°C)</li><br />
          <li style="color: rgb(255,051,000)">Temperatura<br />Esterna (°C)</li><br />
          <li style="color: rgb(000,204,000)">Tensione (V)</li><br />
          <li style="color: rgb(000,153,204)">Pressione (daPa)</li><br />
          <li style="color: rgb(255,153,000)">Altezza (hm)</li><br />
          <li style="color: rgb(000,051,000)">Velocità (km/h)</li><br />
        </ul>
      </div>
    <!--</center>-->
    <img id="logoGraph" class="imageLink" src="img/graph.PNG" onclick="viewGraph()"/>
    <div id='opacityBackground' onclick="hideGraph()"></div>
    <div id="graphBackground">
      <canvas id="graph"></canvas>
    </div>
  </body>
</html>
<?php  
  function isMobile()
  {  
    $array_mobile = array
    (  
      'iphone',  
      'ipod',   
      'ipad',   
      'android',   
      'blackberry',   
      'opera mobi',   
      'windows ce',  
      'windows phone os',  
      'symbian'  
    );  
    $UA = isset($_SERVER['HTTP_USER_AGENT']) ? (string) $_SERVER['HTTP_USER_AGENT'] : '';  
    $regex = "/(" . implode("|", $array_mobile) . ")/i";  
    return preg_match($regex, $UA);   
  }  
  if(isMobile())
  {  
    echo "<script>document.getElementById('logoGraph').style.display = 'none';</script>";  
  }  
?>  
