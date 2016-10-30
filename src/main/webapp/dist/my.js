/**
 * Created by grisi on 08.09.16.
 */

var store;
var from;
var to;
var calc;

$.getJSON("/rest/stores", null, function (data) {
    $("#storeList").append("<option value=''>-</option>");
    $.each(data, function (i, item) {
        $("#storeList").append("<option value=" + item.id + ">" + item.descr + "</option>");
    });
});

$(function () {
    var dateFrom =   $("#fromDatepicker").datepicker({
        dateFormat: 'yy-mm-dd',
        //changeMonth: true,
        //changeYear: true,
        minDate: new Date(2010, 2 - 1, 2),
        onSelect: function (selectedDate) {
            console.log(">>>>>selectedDate1:", selectedDate);
            from = selectedDate;
            updateTimeLine();
            $("#toDatepicker").datepicker("option", "minDate", selectedDate);
        }
    });
   var dateTo = $("#toDatepicker").datepicker({
        dateFormat: 'yy-mm-dd',
        //changeMonth: true,
        //changeYear: true,
        minDate: new Date(2010, 2 - 1, 2),
        onSelect: function (selectedDate) {
            to = selectedDate;

            updateTimeLine();

            //$("#fromDatepicker").datepicker("option", "minDate", selectedDate );
        }
    });

    $('#clearFromDate').on('click', function() {
        document.getElementById('fromDatepicker').value = null;
        $("#toDatepicker").datepicker( "option" , {
            minDate: null,
            maxDate: null});
        from = null;
        updateTimeLine();
    });

    $('#clearToDate').on('click', function() {
        document.getElementById('toDatepicker').value = null
        dateTo.attr('value', '');
        dateTo.datepicker( "option" , {
            minDate: null,
            maxDate: null} );
        to = null;
        updateTimeLine();
    });


    //$("#fromDatepicker").on('blur',function(e){
    //    from = $(this).val();
    //    if(from) {
    //        updateTimeLine();
    //    }
    //    $("#toDatepicker").datepicker("option", "minDate", from);
    //});
    //
    //$("#toDatepicker").on('blur',function(e){
    //    to = $(this).val();
    //    if(to) {
    //        updateTimeLine();
    //    }
   // });
});

$("#storeList").click(function () {
    if ($(this).find("option").size() > 0) {
        store = $(this).val();
        updateCalculations();
        updateTimeLine();
    } else {
        alert("empty");
    }
});



function updateCalculations() {
    $.getJSON("/rest/calculations?store=" + store, null, function (data) {
        $("#calculationList option").remove();
        $("#calculationList").append("<option value=''>-</option>");
        $.each(data, function (i, item) {
            $("#calculationList").append("<option value=" + item.id + ">" + item.interval + "</option>");
        });
    });

}

$("#calculationList").click(function () {
    if ($(this).find("option").size() > 0) {
        calc = $(this).val();
        updateTimeLine();
    } else {
        alert("empty");
    }
});

function updateTimeLine() {
    if (!store || !calc) {
        return
    }
    document.getElementById('loading').style.display = 'true';
    var queryParams = {store: store};
    if (from) {
        queryParams.from = from
    }
    if (to) {
        queryParams.to = to
    }
    if (calc) {
        queryParams.calc = calc
    }
    $.ajax({
        type: 'GET',
        url: '/rest/shifts',
        data: queryParams,
        success: function (data) {
            document.getElementById('loading').style.display = 'none';
            var container = document.getElementById('visualization');
            container.innerHTML = "";
            var timeline = new vis.Timeline(container);
            var groupsData = [];
            var itemsData = [];
            for (var prData in data) {
                var person = data[prData];
                for (var prPerson in person) {
                    if (prPerson == 'shifts') {
                        var shifts = person[prPerson];
                        for (var prShifts in shifts) {
                            var shift = shifts[prShifts];
                            var start = new Date(shift.start + 'T00:00:00+0300');
                            var end = new Date(start.getTime() + 86400000);
                            itemsData.push({
                                id: shift.planId,
                                group: person.id,
                                content: shift.hours + '',
                                start: start,
                                end: end
                            });
                        }
                    }
                }
                groupsData.push({id: person.id, content: person.name, value: 0})
            }

            var groups = new vis.DataSet(groupsData);
            var items = new vis.DataSet(itemsData);

//			var options = {
//				groupOrder: function (a, b) {
//					return a.value - b.value;
//				},
//				editable: true
//			};
            //timeline.setOptions(options);
            timeline.setGroups(groups);
            timeline.setItems(items);
        },
        error: function (err) {
            console.log('Error', err);
            if (err.status === 0) {
                alert('Failed to load data/basic.json.\nPlease run this example on a server.');
            }
            else {
                alert('Failed to load data');
            }
        }
    });
}