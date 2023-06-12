fetch("./route")
	.then((response) => response.json())
	.then((input) => {
		let points = {};
		let lastLoadMs = 0;

		const PATH_SNAP_METER = 100;

		const query = {};
		const queryString = window.location.search;
		const pairs = (queryString[0] === '?' ? queryString.substr(1) : queryString).split('&');
		for (let i = 0; i < pairs.length; i++) {
			let pair = pairs[i].split('=');
			query[decodeURIComponent(pair[0])] = decodeURIComponent(pair[1] || '');
		}

		const startMs = Date.now();
		const baseMs = parseInt(query.ts || startMs);
		const now = () => baseMs + Date.now() - startMs;

		const map = L.map('map', {
			zoomControl: false
		});
		map.setZoom(15);
		L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/light_all/{z}/{x}/{y}{r}.png', {
			attribution: '<a href="&copy; https://github.com/yuzawa-san/miles-per-awa">yuzawa-san</a>, <a href="http://www.openstreetmap.org/copyright">OSM</a>, &copy; <a href="https://carto.com/attribution">CARTO</a>'
		}).addTo(map);

		const $loading = document.getElementById("loading");
		const $navigation = document.getElementById("navigation");
		const $info = document.getElementById("message");
		const selectedLocation = L.marker({}).setLatLng([0, 0]);
		let calculateLatLng = null;
		function setLocation(loc) {
			calculateLatLng = loc;
			$loading.innerText = "loading...";
			$navigation.href = `https://www.google.com/maps/dir/?api=1&travelmode=walking&destination=${calculateLatLng.lat},${calculateLatLng.lng}`;
			selectedLocation.setLatLng(calculateLatLng);
		}
		map.on('click', function(e) {
			setLocation(e.latlng);
		});

		let normalPath = [];
		let {
			rawPath,
			name,
			intervalMeters,
		} = input;
		document.getElementById("name").innerText = name;
		for (let i = 0; i < input.rawPath.length; i += 2) {
			normalPath.push(L.latLng([rawPath[i], rawPath[i + 1]]));
		}
		const routePolyline = L.polyline(normalPath, {
			color: 'rgb(254, 67, 0)',
			weight: 1
		}).addTo(map);
		map.fitBounds(routePolyline.getBounds().pad(0.3));
		setLocation(map.getCenter());

		const locationCircle = L.circle([0, 0], 1);
		locationCircle.addTo(map);
		let locationWatch = null;
		let locationFound = false;
		let $locate = document.getElementById("locate");
		$locate.onclick = function() {
			if (locationFound) {
				const location = locationCircle.getLatLng();
				setLocation(location)
				map.panTo(location);
				return;
			}
			if (!locationWatch) {
				locationWatch = map.locate({
					maximumAge: 20000,
					setView: false,
					watch: true
				});
			}
		};
		function onLocationFound(e) {
			const radius = e.accuracy / 2;
			locationCircle.setLatLng(e.latlng);
			locationCircle.setRadius(radius);
			if (!locationFound && e.accuracy < 250) {
				locationFound = true;
				const location = e.latlng;
				setLocation(location);
				map.panTo(location);
			}
		}
		map.on('locationfound', onLocationFound);
	
		function onLocationError(e) {
			alert(e.message);
		}
	
		map.on('locationerror', onLocationError);
		

		const state = {};

		function latLonForDistance(distance) {
			const targetIndex = distance / intervalMeters;
			const i1 = Math.floor(targetIndex);
			const i2 = i1 + 1;
			if (i2 >= normalPath.length) {
				return normalPath[normalPath.length - 1];
			}
			const pct = targetIndex - i1;
			const p1 = normalPath[i1];
			const p2 = normalPath[i2];
			return [
				p1.lat + (p2.lat - p1.lat) * pct,
				p1.lng + (p2.lng - p1.lng) * pct
			];
		}

		let $metric = document.getElementById("metric");
		$metric.onchange = function() {
			if ($metric.value === 'km'){
				localStorage.setItem("metric","1");
			} else {
				localStorage.removeItem("metric");
			}
			window.location.reload();
		};
		let metric = !!localStorage.getItem("metric");
		if (metric) {
			$metric.value = 'km';
		}
		const labelUnit = metric ? "km" : "mi";
		const labelDistance = metric ? 1000 : 1609;

		function formatDistance(dist) {
			return (dist / labelDistance).toFixed(1);
		}
		const maxDist = normalPath.length * intervalMeters;
		for (let m = 0; m * labelDistance < maxDist; m++) {
			let tooltipLocation = latLonForDistance(m * labelDistance);
			let circle = L.circleMarker(tooltipLocation, {
				radius: 7,
				stroke: false,
				fillOpacity: 0.7,
				color: 'black',
				interactive: false
			});
			circle.addTo(map);

			let text = L.tooltip({
					permanent: true,
					direction: 'center',
					className: 'text',
					interactive: false,
					pane: 'markerPane'
				})
				.setContent(`${m}`)
				.setLatLng(tooltipLocation);
			text.addTo(map);
		}
		selectedLocation.addTo(map);

		const DEG_TO_RAD = 0.0174532925199;
		const RAD_TO_DEG = 57.295779513082320876;
		L.LatLng.prototype.headingTo = function(dest) {

			const lat1 = DEG_TO_RAD * this.lat;
			const lon1 = DEG_TO_RAD * this.lng;
			const lat2 = DEG_TO_RAD * dest.lat;
			const lon2 = DEG_TO_RAD * dest.lng;

			const dlon = lon2 - lon1;
			const dlat = lat2 - lat1;

			const x = Math.cos(lat2) * Math.sin(dlon);
			const y = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dlon);
			let brng = Math.atan2(x, y);
			brng = RAD_TO_DEG * brng;
			if (brng < 0) {
				brng += 360;
			}
			return brng;
		}

		function padZero(t) {
			return t < 10 ? `0${t}` : t;
		}

		function candidates(pos) {
			let lastOffset = -1e9;
			const bins = [];
			const candidates = []
			let prev = null;
			normalPath.map(function(latLng, idx) {
				let h = NaN;
				if (prev) {
					h = prev.headingTo(latLng);
				}
				prev = latLng;
				return {
					latLng: latLng,
					offset: idx * intervalMeters,
					distance: latLng.distanceTo(pos),
					h
				};
			}).filter(function(x) {
				return x.distance < PATH_SNAP_METER;
			}).forEach(function(x) {
				if (Math.abs(lastOffset - x.offset) > intervalMeters) {
					bins.push([]);
				}
				bins[bins.length - 1].push(x)
				lastOffset = x.offset;
			});
			return bins.map(function(bin) {
				const sortedBin = bin.sort(function(a, b) {
					return a.distance - b.distance;
				});
				return sortedBin[0];
			})
		}

		function render() {
			const nowMs = now();
			if ((nowMs - lastLoadMs) > 15000) {
				lastLoadMs = nowMs;
				fetch("./locations", {
						method: 'POST',
						headers: {
							'Content-Type': 'application/json'
						},
						body: JSON.stringify({
							people: null
						})
					})
					.then((response) => response.json())
					.then((input) => {
						input.people.forEach(v => {
							points[v.name] = v;
						});
					});
			}

			for (let name in points) {
				const point = points[name];
				let userState = state[name];
				if (!userState) {
					const marker = L.tooltip({
						permanent: true
					}).setLatLng([0, 0]).setContent(name);
					marker.addTo(map);
					userState = {
						name,
						marker
					}
					state[name] = userState;
				}
				userState.v = point.velocity;
				userState.estimatedOffset = Math.min(maxDist, (point.index * intervalMeters) + (nowMs - point.timestampMs) / 1000 * point.velocity);
				userState.marker.setLatLng(latLonForDistance(userState.estimatedOffset));

			}


			if (calculateLatLng) {
				const targets = candidates(calculateLatLng);
				let out = `<table border=1><tr><th rowspan=2>name</th><th rowspan=2>${labelUnit}</th><th rowspan=2>pace</th>` + targets.map(dst => `<th colspan=2>to ${labelUnit} ${formatDistance(dst.offset)}</th>`).join("") + "</tr>";
				out += "<tr>"+ targets.map(dst => `<th>in</th><th>at</th>`).join("") + "</tr>";
				for (let name in state) {
					const userState = state[name];
					if (userState.estimatedOffset) {
						const paceSeconds = labelDistance / userState.v;
						out += `<tr><td>${name}</td><td>${formatDistance(userState.estimatedOffset)}</td><td>${Math.floor(paceSeconds / 60)}&#8242;${padZero(Math.floor(paceSeconds % 60))}&#8243;</td>`
					} else {
						continue;
					}
					targets.forEach(dst => {
						const deltaD = dst.offset - userState.estimatedOffset;
						if (deltaD < 0) {
							out += `<td colspan=2>passed</td>`;
							return;
						}
						const deltaT = deltaD / userState.v;
						const dt = new Date(nowMs + deltaT * 1000);
						const etaMinutes = Math.floor(deltaT / 60);
						const etaSeconds = padZero(Math.floor(deltaT % 60));
						out += `<td>${etaMinutes}&#8242;${etaSeconds}&#8243;</td><td>${padZero(dt.getHours())}:${padZero(dt.getMinutes())}</td>`;
					});
					out += '</tr>';
				}
				out += '</table>';
				if (targets.length == 0) {
					$loading.innerText = "no targets, please click on route";
				} else {
					$loading.innerHTML = "&nbsp;";
				}
				$info.innerHTML = out;
			}
		}
		render();
		setInterval(render, 1000);
	});
