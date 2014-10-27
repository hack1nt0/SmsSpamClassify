/**
 * Created by root on 14-8-6.
 */

var fs = require('fs');
var iIn = fs.createReadStream('/home/dy/IdeaProjects/SmsSpamClassify/data/sms_20140806.json');
//iIn.read

fs.readFile('/home/dy/IdeaProjects/SmsSpamClassify/data/sms_20140826.json', 'utf8', function (err, data) {
    if (err) throw err;
    var iOut = fs.createWriteStream("/home/dy/IdeaProjects/SmsSpamClassify/data/sms_20140826.txt");
    var smsinfos = JSON.parse(data).info.smsinfo;

    function isPhoneNO(str) {
        var pattern = /(\+\d+)?(\d+[-])?\d+([-]\d+)?/;
        var match = pattern.exec(str);
        //console.log(match)
        if (match != null) {
            var start = match.index;
            var text = match[0];
            var end = start + text.length;
            return end == str.length;
        }
        return false;
    }

    function write2File(ele) {
        //out.writeln("{\"body\": \"" + ele.sms + "\", \"address\": \"" + ele.phonenum + "\"");
        console.log(JSON.stringify(ele));
        iOut.write(JSON.stringify(ele) + "\n");
    }


    for (var i = 0; i < smsinfos.length; ++i) {
        var smsList;

        try {
            smsList = JSON.parse(smsinfos[i].smsinfo);
            smsList = (typeof smsList.smslist != 'undefined') ? JSON.parse(smsList.smslist) : smsList;
        } catch (e) {
            continue;
        }
        for (var j = 0; j < smsList.length; ++j) {
            if (typeof smsList[j].sms != 'undefined' && typeof smsList[j].phonenum != 'undefined') {
                //console.log(smsList[j])
                write2File({
                    address: smsList[j].phonenum,
                    body: smsList[j].sms
                });
            } else {
                Object.keys(smsList[j]).forEach(function(key) {
                    if (isPhoneNO(key))
                        write2File({
                            address: key,
                            body: smsList[j][key]
                        });
                });
            }
        }
    }
});

