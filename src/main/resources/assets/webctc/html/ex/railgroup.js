let currentRailElems = [];
let key_shift;

const RAIL_GROUP_BASE_URL = `${protocol}//${host}/api/railgroups/`

document.addEventListener('DOMContentLoaded', async () => {
  let svg = document.getElementById('map');
  let g = document.getElementById('mtx');
  try {
    await updateRail(g)
    Array.from(document.querySelectorAll("[id^='rail,']")).forEach(rail => {
      rail.setAttribute('stroke', 'white')
    })
  } catch (e) {
    alert("Error!" + "\n" + e);
    return;
  }

  svg.onclick = e => selectRail(e.target.parentElement)

  document.onkeydown = event => {
    let key_event = event || window.event;
    key_shift = (key_event.shiftKey);
  }

  document.onkeyup = event => {
    let key_event = event || window.event;
    key_shift = (key_event.shiftKey);
  }

  fetchData()
    .then(json => json.forEach(rg => addRailGroupElement(rg)))
    .then(() => sortRailGroup())

  panzoom(g, {smoothScroll: false})

  let ws;
  $(() => {
    $('.js-open').click(() => {
      let uuid = $("#rg_uuid").val()
      if (!uuid) {
        return;
      }
      $('#overlay, .modal-window').fadeIn();
      ws = new WebSocket(`ws://${host}/api/railgroups/BlockPosConnection`);
      ws.onmessage = event => {
        let data = JSON.parse(event.data);
        let li = this.createPosElement(data['x'], data['y'], data['z']);
        $(li).hide()
        document.getElementById("modal_pos_list").appendChild(li)
        $(li).fadeIn()
      }
    });
    $('.js-close').click(() => {
      ws.close();
      let list = $("#modal_pos_list")
      let input = list.find("input")
      for (let i = 0; i < input.length; i += 3) {
        let x = input.eq(i).val()
        let y = input.eq(i + 1).val()
        let z = input.eq(i + 2).val()
        this.addPos(x, y, z)
      }
      list.empty();
      $('#overlay, .modal-window').fadeOut()
    });
  });
});

function setRailActive(rail, b) {
  if (rail.getAttribute('stroke') === (b ? 'white' : 'lightblue')) {
    rail.setAttribute('stroke', (!b ? 'white' : 'lightblue'));
  }
}

async function fetchData(location = null, data = null) {
  let url = createURL(location, data)
  return await fetch(url)
    .then(res => res.json())
}

async function postData(location = null, data = null) {
  let url = createURL(location, data)
  return await fetch(url, {method: 'POST',})
    .then(res => res.json())
}

function createURL(location, data) {
  let url = RAIL_GROUP_BASE_URL
  if (location != null) {
    url += location
  }
  if (data != null) {
    url += ("?" + new URLSearchParams(data))
  }
  return url;
}

function hoverListItem(elem, b) {
  let span;
  if (elem.tagName === "LI") {
    span = elem.children[0]
  } else if (elem.tagName === "SPAN") {
    span = elem
  }
  if (span != null) {
    let split = span.innerText.split(",")
    let id = "rail," + split[0] + "," + split[1] + "," + split[2] + ","
    let line = document.getElementById(id)
    if (line != null) {
      line.setAttribute("stroke", b ? "green" : "orange")
    }
  }
}


function addRailGroupElement(json) {
  let li = document.createElement("li")
  li.className = "list-group-item"
  li.id = json["uuid"]
  li.innerText = json["name"] + " (" + json["railPosList"].length + "rails)"
  li.onclick = e => selectRailGroup(e.target, null)
  document.getElementById("rg_list").appendChild(li)
  return li;
}

function sendRailGroup() {
  let uuid = document.getElementById("rg_uuid").value
  let name = document.getElementById("rg_name").value
  if (!uuid || !name) {
    return;
  }
  Array.from(document.getElementById("rs_pos_list").children).forEach(li => {
    let is = li.children[0].children
    let x = is[0].value
    let y = is[1].value
    let z = is[2].value
    if (x && y && z) {
      postData("add", {"uuid": uuid, "x": x, "y": y, "z": z, "rs": true})
    }
  })
  postData("update", {"uuid": uuid, "name": name})
    .then(json => updateRailGroup(json))
    .then(() => sortRailGroup())
}

function selectRailGroup(elem, json) {
  Array.from(document.getElementById("rg_list").children).forEach(elm =>
    elm.style.backgroundColor = "white")
  elem.style.backgroundColor = "lightblue"
  if (json == null) {
    fetchData("railgroup", {"uuid": elem.id})
      .then(json => updateRailGroup(json))
  } else {
    updateRailGroup(json)
  }
}

function updateRailGroup(json) {
  document.querySelectorAll("[id^='rail']").forEach(group => {
    if (group.getAttribute('stroke') === 'orange') {
      group.setAttribute('stroke', 'white');
    }
  })

  document.getElementById("rg_name").value = json["name"]
  document.getElementById("rg_uuid").value = json["uuid"]
  let rails = document.getElementById("rg_rails")
  rails.innerHTML = ""
  json["railPosList"].forEach(pos => {
    let id = "rail," + pos["x"] + "," + pos["y"] + "," + pos["z"] + ","
    let rg = document.getElementById(id)
    if (rg != null) {
      rg.setAttribute('stroke', 'orange')
    }

    let li = document.createElement("li")
    li.className = "list-group-item ex-list-item"
    let span = document.createElement("span")
    span.className = "ex-list-item-span"
    span.innerText = pos["x"] + "," + pos["y"] + "," + pos["z"]
    let button = document.createElement("button")
    button.className = "btn btn-outline-danger ex-button"
    button.innerText = "Remove"
    button.onclick = e => removeRail(e.target)
    li.onmouseover = e => hoverListItem(e.target, true)
    li.onmouseout = e => hoverListItem(e.target, false)
    li.appendChild(span)
    li.appendChild(button)
    rails.appendChild(li)
  })

  let li = document.getElementById(json["uuid"])
  li.innerText = json["name"] + " (" + json["railPosList"].length + "rails)"

  document.getElementById("rs_pos_list").innerHTML = ""
  json["rsPosList"].forEach(pos => {
    addPos(pos["x"], pos["y"], pos["z"])
  })
}

function createRailGroup() {
  postData("create")
    .then(json => {
      let li = addRailGroupElement(json);
      selectRailGroup(li, json);
    })
}

function addRail() {
  let uuid = document.getElementById("rg_uuid").value
  if (!uuid) {
    return;
  }
  currentRailElems.forEach(currentRailElem => {
    let split = currentRailElem.id.split(",")
    postData("add", {"uuid": uuid, "x": split[1], "y": split[2], "z": split[3]})
      .then(json => updateRailGroup(json))
  })
  currentRailElems.splice(0)
}

function removeRail(elem) {
  let split = elem.parentElement.children[0].innerText.split(",")
  let uuid = document.getElementById("rg_uuid").value
  postData("remove", {"uuid": uuid, "x": split[0], "y": split[1], "z": split[2]})
    .then(json => updateRailGroup(json))
}

function deleteRailGroup() {
  let uuid = document.getElementById("rg_uuid").value
  if (!uuid) {
    return;
  }

  postData("delete", {"uuid": uuid})
    .then(json => {
      if (json["removed"]) {
        document.getElementById(uuid).remove();
        document.getElementById("rg_name").value = ""
        document.getElementById("rg_uuid").value = ""
        document.getElementById("rg_rails").innerHTML = ""
      }
    })

  document.querySelectorAll("[id^='rail']").forEach(group => {
    if (group.getAttribute('stroke') === 'orange') {
      group.setAttribute('stroke', 'white');
    }
  })
}

async function selectRail(elem) {
  let id = elem.id
  if (id.startsWith("rail,")) {
    if (key_shift) {
      if (currentRailElems.includes(elem)) {
        setRailActive(elem, false)
        let i = currentRailElems.indexOf(elem)
        currentRailElems.splice(i, 1)
      } else {
        setRailActive(elem, true)
        currentRailElems.push(elem)
      }
    } else {
      if (currentRailElems.length === 0) {
        setRailActive(elem, true)
        currentRailElems.push(elem)
      } else if (currentRailElems.length === 1) {
        if (currentRailElems[0] === elem) {
          setRailActive(elem, false)
          currentRailElems.splice(0)
        } else {
          setRailActive(currentRailElems[0], false)
          currentRailElems.splice(0)
          setRailActive(elem, true)
          currentRailElems.push(elem)
        }
      } else {
        document.querySelectorAll("[id^='rail']").forEach(group => {
          setRailActive(group, false)
        })
        currentRailElems.splice(0)
        setRailActive(elem, true)
        currentRailElems.push(elem);
      }
    }
  }
}

function addPos(xCoord = "", yCoord = "", zCoord = "") {
  let uuid = document.getElementById("rg_uuid").value
  if (!uuid) {
    return;
  }
  let li = this.createPosElement(xCoord, yCoord, zCoord)
  document.getElementById("rs_pos_list").appendChild(li)
}

function createPosElement(xCoord, yCoord, zCoord) {
  let li = document.createElement("li")
  li.className = "list-group-item ex-list-item"
  let label = document.createElement("label")
  let x = createPosInputElement("X")
  x.value = xCoord
  label.appendChild(x)
  let y = createPosInputElement("Y")
  y.value = yCoord
  label.appendChild(y)
  let z = createPosInputElement("Z")
  z.value = zCoord
  label.appendChild(z)
  li.appendChild(label)
  let button = document.createElement("button")
  button.className = "btn btn-outline-danger ex-button"
  button.innerText = "Remove"
  button.onclick = e => li.parentElement.id === "rs_pos_list" ? removePos(e.target.parentElement) : li.remove()
  li.appendChild(button)
  return li
}

function createPosInputElement(placeholder) {
  let input = document.createElement("input")
  input.className = "ex-cord-input"
  input.type = "number"
  input.placeholder = placeholder;
  return input
}

function removePos(elem) {
  let uuid = document.getElementById("rg_uuid").value
  if (!uuid) {
    return;
  }
  let is = elem.children[0].children
  let x = is[0].value
  let y = is[1].value
  let z = is[2].value
  if (x && y && z) {
    postData("remove", {"uuid": uuid, "x": x, "y": y, "z": z, "rs": true})
  }
  elem.remove()
}

function filterRailGroup(elem) {
  let split = !elem.value.replace("　", " ").split(" ")
  Array.from(document.getElementById("rg_list").children)
    .forEach(li => li.classList.toggle("nodisplay", !split.every(s => li.innerText.includes(s))))
}

function sortRailGroup() {
  $('#rg_list').each(function () {
    return $(this).html($(this).find('li').sort((a, b) => $(a).text() > $(b).text() ? 1 : -1));
  });
}