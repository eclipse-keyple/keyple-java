<%@ page language="java" contentType="text/html; charset=UTF-8"
 pageEncoding="UTF-8"%>
<%
    response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "0");
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<c:set var="ctx" value="${pageContext['request'].contextPath}" />
<html>

<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>Application de test CALYPSO</title>
<link href="/css/jquery-ui.min.css" rel="stylesheet">
<link href="/css/bootstrap.min.css" rel="stylesheet">

</head>
<body>


 <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->

 <script src="/js/jquery-3.1.1.min.js"></script>
 <script src="/js/jquery-ui.min.js"></script>
 <!-- Include all compiled plugins (below), or include individual files as needed -->
 <script src="/js/bootstrap.min.js"></script>
 <script src="/js/Chart.bundle.js"></script>

 <script>
        $(function() {
            $(".alert").hide();

            $("#openSessionButton").click(function(event) {
                $("#message").hide().empty();
                $.ajax({
                    method : "GET",
                    data : {
                        sfiToSelect : $("#sfiToSelect option:selected").val(),
                    },
                    url : "/openSession/" + $("#sfiToSelect option:selected").val() + "/",
                }).done(function(msg) {
                    $("#message").show();
                    $("#message").text(msg);
                    if (msg == "Successful execution.,Successful execution.") {
                        $("#message").css("background-color", "#33cc33");
                    } else if (msg === "No Card") {
                        $("#message").css("background-color", "#ff0000");
                    } else if (msg === "veuillez vérifier les lecteurs") {
                        $("#message").css("background-color", "#ff0000");
                        $("#message").css("color", "#000000");
                    } else {
                        $("#message").css("background-color", "#ff0000");
                    }
                });

            });

            $("#closeSessionButton").click(function(event) {
                $("#message").hide().empty();
                $.ajax({
                    method : "GET",
                    url : "/closeSession",
                }).done(function(msg) {
                    $("#message").show();
                    $("#message").text(msg);
                    if (msg == "Successful execution.") {
                        $("#message").css("background-color", "#33cc33");
                    } else if (msg === "No Card") {
                        $("#message").css("background-color", "#ff0000");
                    } else {
                        $("#message").css("background-color", "#ff0000");
                    }
                });
            });
            $("#cancelSessionButton").click(function(event) {
                $("#message").hide().empty();
                $.ajax({
                    method : "GET",
                    url : "/cancelSession",
                }).done(function(msg) {
                    $("#message").show();
                    $("#message").text(msg);
                    if (msg == "Successful execution.") {
                        $("#message").css("background-color", "#4126ad");
                    } else if (msg === "No Card") {
                        $("#message").css("background-color", "#ff0000");
                    } else {
                        $("#message").css("background-color", "#ff0000");
                    }
                });
            });
            $("#getDataFciButton").click(function(event) {
                $("#message").hide().empty();
                getErrorMsg()
                $.ajax({
                    method : "GET",
                    url : "/getDataFci",
                }).done(function(msg) {
                    $("#message").show();
                    $("#message").text(msg);
                    if (msg === "Successful execution.") {
                        $("#message").css("background-color", "#33cc33");
                    } else if (msg === "No Card") {
                        $("#message").css("background-color", "#ff0000");
                    } else {
                        $("#message").css("background-color", "#ff9933");
                    }
                });
            });

            $("#getVersionButton").click(function(event) {
                $("#message").hide().empty();
                $.ajax({
                    method : "GET",
                    url : "/getVersion",
                }).done(function(msg) {
                    $("#message").show();
                    $("#message").text(msg);
                    if (msg === "R2_4") {
                        $("#message").css("background-color", "#33cc33");
                    } else if (msg === "R3_1") {
                        $("#message").css("background-color", "#33cccc");
                    } else if (msg === "") {
                        $("#message").css("background-color", "#ff0000");
                        $("#message").text("NO CARD");
                    } else {
                        $("#message").css("background-color", "#ff9933");
                    }
                });
            });

            $("#getListEF").click(
                    function(event) {
                        $("#message").hide().empty();
                        $.ajax({
                            method : "GET",
                            url : "/listEF",
                        }).done(
                                function(msg) {
                                    $('#listEF').empty();
                                    $.each(msg, function(i, item) {
                                        $('#listEF').append(
                                                $('<option>', {
                                                    value : item.sfi,
                                                    text : 'filetype: ' + toHex(item.fileType) + ' (' + item.fileTypeName + ') lid: ' + toHex(item.lid) + ' sfi: ' + toHex(item.sfi) + ' (' + item.sfiName + ') recSize: ' + toHex(item.recSize)
                                                            + ' numberRec: ' + toHex(item.numberRec)
                                                }));
                                    });
                                });
                    });
            $("#getData").click(function(event) {
                $("#message").hide().empty();
                var cmd = getCmd();
                $.ajax({
                    method : "GET",
                    data : {
                        dataType : $("#dataType option:selected").val(),
                    },
                    url : "/getData/" + $("#dataType option:selected").val() + "/",
                }).done(function(msg) {
                    console.log(msg.length);
                    $("#message").text("");
                    $("#message").show();
                    $("#message").text(msg);
                    $("#message").css("background-color", "#00000");
                    $("#message").css("color", "#fffff");
                    $("#message").css("color", "#fffff");
                });
            });

            $("#infoButton").click(function(event) {
                $("#message").hide().empty();
                var cmd = getCmd();
                console.log(cmd);
                console.log($("#numberRec").val());
                $.ajax({
                    method : "GET",
                    data : {
                        file : $("#listEF option:selected").val(),
                        numberRec : $("#numberRec").val(),
                        enumCmd : cmd
                    },
                    url : "/info/" + $("#listEF option:selected").val() + "/" + $("#numberRec").val() + "/" + cmd + "/",
                }).done(function(msg) {
                    console.log(msg.length);
                    $("#message").text("");
                    $("#message").show();
                    $("#message").text(msg);
                    $("#message").css("background-color", "#00000");
                    $("#message").css("color", "#fffff");
                    $("#message").css("color", "#fffff");
                });
            });

            $("#updateRecord").click(function(event) {
                $("#message").hide().empty();
                var cmd = getCmd();
                console.log(cmd);
                $.ajax({
                    method : "POST",
                    data : {
                        stringRecordToWrite : $('#stringRecordToWrite').val(),
                        file : $("#listEF option:selected").val(),
                        numberRec : $("#numberRec").val(),
                        enumCmd : cmd
                    },
                    url : "/updateRecord/" + $('#stringRecordToWrite').val() + "/" + $("#listEF option:selected").val() + "/" + $("#numberRec").val() + "/" + cmd + "/",
                }).done(function(msg) {
                    $("#message").show();
                    $("#message").text(msg);
                    if (msg === "Card updated ") {
                        $("#message").text(msg + $('#stringRecordToWrite').val() + " Correctly written on record number " + $("#numberRec").text() + ", please Close session to validate");
                        $("#message").css("background-color", "#33cc33");
                    } else {
                        $("#message").css("background-color", "#ff0000");
                    }
                    $('#stringRecordToWrite').val("");
                });
            });

            $("#getRecord").click(function(event) {
                $("#message").hide().empty();
                $.ajax({
                    method : "POST",
                    data : {
                        sfi : $('#listEF option:selected').val()
                    },
                    url : "/ReadRecord",
                }).done(function(msg) {
                    alert(toHex(msg.value));
                });
            });
            $("#readinSession").click(function(event) {
                $("#message").hide().empty();
                readInSession();
            });

            function readInSession() {
                $.ajax({
                    method : "POST",
                    data : {
                        sfi : $('#sfiToRead option:selected').val()
                    },
                    url : "/ReadInSession",
                }).done(function(msg, status, request) {
                    $("#message").show();
                    $("#message").text(msg + " in " + request.getResponseHeader('processtime') + "ms");
                    $("#message").css("background-color", "#33cc33");
                });
            }
            $("#waitAndReadinSession").click(function(event) {
                $("#message").hide().empty();
                doneWaitAndReadinSession = false;
                $("#waitAndReadinSession").text("waiting for card ...");
                waitForCardPresenceAndRead();
            });

            var doneWaitAndReadinSession = false;
            function waitForCardPresenceAndRead() {
                $.ajax({
                    method : "POST",
                    url : "/isCardPresent",

                }).done(function(msg, status, request) {
                    if (msg == "true") {
                        doneWaitAndReadinSession = true;
                        var video_play = $('#audiotag1');
                        video_play.on('canplay', function() {
                            video_play.trigger('play');
                        });
                        readInSession();

                        $("#waitAndReadinSession").text("Read at card presentaiton");
                    } else {
                        waitForCardPresenceAndRead();
                    }

                });

            }

            $("#readMonitoringChart").hide();
            var serieMonitorReadinSession = [];
            var startMonitorReadinSession = false;
            var i;
            var xMonitorReadinSession = [];
            $("#monitorReadinSession").click(function(event) {
                $("#message").hide().empty();
                if (startMonitorReadinSession) {
                    startMonitorReadinSession = false;
                    $("#monitorReadinSession").text("Monitor");
                } else {
                    startMonitorReadinSession = true;
                    i = 0;
                    var readMonitoringChart = new Chart($("#readMonitoringChart"), {
                        type : 'line',
                        data : {
                            labels : xMonitorReadinSession,
                            datasets : [ {
                                label : 'process time in ms',
                                data : serieMonitorReadinSession,
                            } ]
                        },
                        animation : {
                            animateScale : true
                        },

                    });
                    $("#readMonitoringChart").show();
                    $("#monitorReadinSession").text("Stop Monitor");
                    updateMonitorReadInSession(readMonitoringChart);
                }

            });
            function updateMonitorReadInSession(readMonitoringChart) {
                $.ajax({
                    method : "POST",
                    data : {
                        sfi : $('#sfiToRead option:selected').val()
                    },
                    url : "/ReadInSession",
                }).done(function(msg, status, request) {
                    serieMonitorReadinSession.push(request.getResponseHeader('processtime'));
                    xMonitorReadinSession.push(i++);
                    readMonitoringChart.labels = xMonitorReadinSession;
                    readMonitoringChart.reset();
                    readMonitoringChart.update();
                    if (startMonitorReadinSession) {
                        updateMonitorReadInSession(readMonitoringChart);
                    }
                });
            }

            $("#writeinSession").click(function(event) {
                $("#message").hide().empty();
                writeinSession();
            });

            function writeinSession() {
                $.ajax({
                    method : "POST",
                    data : {
                        sfi : $('#SfiTowrite option:selected').val(),
                        data : $('#waitDataToWrite').val()
                    },
                    url : "/WriteInSession",
                }).done(function(msg, status, request) {
                    $("#message").show();
                    $("#message").text(msg + " in " + request.getResponseHeader('processtime') + "ms");
                    $("#message").css("background-color", "#33cc33");
                });
            }

            $("#waitAndWriteinSession").click(function(event) {
                $("#message").hide().empty();
                doneWaitAndWriteinSession = false;
                $("#waitAndWriteinSession").text("waiting for card ...");
                waitForCardPresenceAndWrite();
            });

            var doneWaitAndWriteinSession = false;
            function waitForCardPresenceAndWrite() {
                $.ajax({
                    method : "POST",
                    url : "/isCardPresent",

                }).done(function(msg, status, request) {
                    if (msg == "true") {
                        doneWaitAndWriteinSession = true;
                        var video_play = $('#audiotag1');
                        video_play.on('canplay', function() {
                            video_play.trigger('play');
                        });
                        writeinSession();

                        $("#waitAndWriteinSession").text("Write at card presentation");
                    } else {
                        waitForCardPresenceAndWrite();
                    }

                });
            }
            
            $("#writeMonitoringChart").hide();
            var serieMonitorWriteinSession = [];
            var startMonitorWriteinSession = false;
            var xMonitorWriteinSession = [];
            $("#monitorWriteinSession").click(function(event) {
                $("#message").hide().empty();
                if (startMonitorWriteinSession) {
                    startMonitorWriteinSession = false;
                    $("#monitorWriteinSession").text("Monitor");
                } else {
                    startMonitorWriteinSession = true;
                    i = 0;
                    var writeMonitoringChart = new Chart($("#writeMonitoringChart"), {
                        type : 'line',
                        data : {
                            labels : xMonitorWriteinSession,
                            datasets : [ {
                                label : 'process time in ms',
                                data : serieMonitorWriteinSession,
                            } ]
                        },
                        animation : {
                            animateScale : true
                        },

                    });
                    $("#writeMonitoringChart").show();
                    $("#monitorWriteinSession").text("Stop Monitor");
                    updateMonitorWriteInSession(writeMonitoringChart);
                }

            });
            function updateMonitorWriteInSession(writeMonitoringChart) {
                $.ajax({
                    method : "POST",
                    data : {
                        sfi : $('#SfiTowrite option:selected').val(),
                        data : $('#waitDataToWrite').val()
                    },
                    url : "/WriteInSession",
                }).done(function(msg, status, request) {
                    serieMonitorWriteinSession.push(request.getResponseHeader('processtime'));
                    xMonitorWriteinSession.push(i++);
                    writeMonitoringChart.labels = xMonitorWriteinSession;
                    writeMonitoringChart.reset();
                    writeMonitoringChart.update();
                    if (startMonitorWriteinSession) {
                        updateMonitorWriteInSession(writeMonitoringChart);
                    }
                });
            }
        });

        function getErrorMsg() {
            var msgError = "no file selected"
            if ($('#listEF').empty()) {
                $("#message").show();
                $("#message").text(msgError);
                $("#message").css("background-color", "#ff9933");
            } else {
                $("#message").show();
                $("#message").text("");
            }
        }

        function toHex(ints) {
            var hexstr = "";
            if (Array.isArray(ints)) {

                for (var i = 0; i < ints.length; i++) {
                    hexstr += toHex(ints[i]);
                }
                return hexstr;

            } else {

                if (ints < 16) {
                    hexstr += "0";
                }
                hexstr += ints.toString(16);
            }
            return hexstr;
        }

        function getCmd() {
            var cmdString = "";
            if ($("#readOneRecordFromEFUsingSfi").is(':checked')) {
                $("#readOneRecordFromEFUsingSfi").text("readOneRecordFromEFUsingSfi");
                cmdString = $("#readOneRecordFromEFUsingSfi").text();
            } else if ($("#readOneRecord").is(':checked')) {
                $("#readOneRecord").text("readOneRecord");
                cmdString = $("#readOneRecord").text();
            } else if ($("#readRecords").is(':checked')) {
                $("#readRecords").text("readRecords");
                cmdString = $("#readRecords").text();
            } else if ($("#readRecordsFromEFUsingSfi").is(':checked')) {
                $("#readRecordsFromEFUsingSfi").text("readRecordsFromEFUsingSfi");
                cmdString = $("#readRecordsFromEFUsingSfi").text();
            }
            return cmdString;
        }
    </script>

 <div class="container theme-showcase" role="main">
  <div class="jumbotron">
   <h1>Application de test CALYPSO</h1>
   <p>Utilisation du SDK Calypso</p>
  </div>

  <div class="page-header">
   <h1></h1>
  </div>

  <ul class="nav nav-pills" role="tablist">
   <li class="active"><a data-toggle="tab" href="#accueil">Accueil</a></li>
   <li><a data-toggle="tab" href="#session">Session</a></li>
   <li><a data-toggle="tab" href="#readwrite">Lecture/Ecriture</a></li>
   <li><a data-toggle="tab" href="#bulk">Bulk actions</a></li>
   <li><a data-toggle="tab" href="#version">Version</a></li>

  </ul>

  <div class="alert alert-success" role="alert" id="message"></div>
  <div class="tab-content">
   <div id="accueil" class="tab-pane fade in active">
    <h3>Accueil</h3>

    <p>Présentation des fonctionnalités.</p>
   </div>
   <div id="session" class="tab-pane fade">
    <h3>Sessions</h3>
    <div id="sfi">
     SFI to select: <select id="sfiToSelect">
      <option value="07">SFI: 07h</option>
      <option value="08">SFI: 08h</option>
      <option value="1E">SFI: 1Eh</option>
      <option value="09">SFI: 09h</option>
      <option value="19">SFI: 19h</option>
      <option value="1D">SFI: 1Dh</option>
      <option value="05">SFI: 05h (Rev 3 card)</option>
     </select><br> <br>
     <button class="btn" id="openSessionButton">open session</button>
     <button class="btn" id="closeSessionButton">close session</button>
     <br> <br>
     <button class="btn" id="cancelSessionButton">cancel
      session</button>
    </div>
   </div>

   <div id="readwrite" class="tab-pane fade">
    <h3>Lecture /Ecriture</h3>
    <select id="listEF"></select>
    <button class="btn" id="getListEF">
     Get List File <span class="glyphicon glyphicon-refresh"></span>
    </button>
    <br> <br>
    <div id="dataType">
     data type <select id="dataType">
      <option value="AidOfCurrentDF">AID of the current DF.</option>
      <option value="fcpForTheCurrentFile">FCP for the current
       file.</option>
      <option value="fciTemplate">FCI for the current DF.</option>
      <option value="listOfEF">EF List of the current DF.</option>
      <option value="traceabilityInformation">Traceability
       information.</option>
      <option value="historicalbytes">Historical bytes of the
       ATR.</option>

     </select>
     <button class="btn" id="getData">Get Data</button>
     <br> <br>
    </div>
    <div>
     Record To read/update: . <input type="text" id="numberRec"
      style="text-align: center" /><br> <br> <input
      id="readOneRecord" type="radio" name="group1" onClick="getCmd()" />read
     One Record<br> <input id="readRecords" type="radio"
      name="group1" onClick="getCmd()" />read Records<br> <input
      id="readOneRecordFromEFUsingSfi" type="radio" name="group1" />read
     One Record From EF Using Sfi<br> <input
      id="readRecordsFromEFUsingSfi" type="radio" name="group1"
      onClick="getCmd()" />read Records From EF Using Sfi<br>

     <button class="btn" id="infoButton">Read infos</button>
    </div>



    <div>
     Record to update (data to be inserted into the card): <input
      type="text" id="stringRecordToWrite"><br>
     <button class="btn" id="updateRecord">Update Record</button>
    </div>
   </div>

   <div id="version" class="tab-pane fade">
    <h3>Version</h3>
    <button class="btn" id="getVersionButton">Get Version</button>

   </div>
   <div id="bulk" class="tab-pane fade">
    <h3>Bulk Actions</h3>
    <div>
     SFI to select: <select id="sfiToRead">
      <option value="07">SFI: 07h</option>
      <option value="08">SFI: 08h</option>
      <option value="1E">SFI: 1Eh</option>
      <option value="09">SFI: 09h</option>
      <option value="19">SFI: 19h</option>
      <option value="1D">SFI: 1Dh</option>
      <option value="05">SFI: 05h (Rev 3 card)</option>
     </select><br> <br>
     <canvas id="readMonitoringChart" width="100" height="40"></canvas>
     <button class="btn" id="readinSession">Read</button>
     <audio id="audiotag1" src="sounds/DING.wav" preload="auto"></audio>
     <button class="btn" id="waitAndReadinSession">Read at card
      presentation</button>
     <button class="btn" id="monitorReadinSession">Monitor</button>
    </div>

    <div style="padding-top: 20px">
     SFI to select: <select id="SfiTowrite">
      <option value="07">SFI: 07h</option>
      <option value="08">SFI: 08h</option>
      <option value="1E">SFI: 1Eh</option>
      <option value="09">SFI: 09h</option>
      <option value="19">SFI: 19h</option>
      <option value="1D">SFI: 1Dh</option>
      <option value="05">SFI: 05h (Rev 3 card)</option>
     </select><br> <br> Data to write : <input type="text"
      id="waitDataToWrite"><br> <br>
     <canvas id="writeMonitoringChart" width="100" height="40"></canvas>
     <button class="btn" id="writeinSession">Read and Write</button>
     <button class="btn" id="waitAndWriteinSession">Write at
      card presentation</button>
     <button class="btn" id="monitorWriteinSession">Monitor</button>

    </div>


   </div>
  </div>



 </div>
</body>
</html>

