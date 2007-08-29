/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2007, ThoughtWorks, Inc.
 * 200 E. Randolph, 25th Floor
 * Chicago, IL 60601 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
var StatisticsObserver = Class.create();
StatisticsObserver.prototype = {
	initialize:function() { },
	notify : function(json) {
		if (!json.length) return;
	    if (json.length == 0) return; 
		var statistics = $H({passed:0,failed:0,building:0})
		for (var i = 0; i < json.length; i++) {
			if (!json[i]) continue;
	    	this.category(json[i], statistics);
	    }
		this.inactive_projects(statistics);
		this.calculate(statistics);
	  	this.update(statistics);
	},
	inactive_projects : function (statistics) {
		statistics['inactive'] = $A($$('.inactive.bar')).size()
	},
	calculate : function (statistics) {
		var total = 0;
		statistics.each(function(pair){
			total += pair.value;
		});
		statistics['total'] = total;
	    var rate = ((statistics['passed'] / (total - statistics['inactive'])) * 100).toFixed(0)
		statistics['rate'] = isNaN(rate) ? "0%" : rate+"%";
		return statistics; 
	},
	category : function (json, statistics) {
		if (!json) return;
		if (!json.building_info) return;
		if (!json.building_info.building_status) return;
		var status = json.building_info.building_status.toLowerCase();
		if (status == 'inactive') return;
		statistics[status] +=  1;
	},
	update : function (statistics_infos) {
		var infos = $A(['passed', 'failed', 'building', 'total', 'rate', 'inactive'])
		infos.each(function(info) {
		    var statistic = $('statistics_' + info).innerHTML;
			$('statistics_' + info).update(statistics_infos[info] + statistic.substring(statistic.indexOf(' ')));
		});
	}
}