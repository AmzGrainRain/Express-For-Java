function testGET() {
  const requestOptions = {
    method: "GET",
    redirect: "follow",
  };

  fetch("http://127.0.0.1:80/test-get?a=1&b=2", requestOptions)
    .then((response) => response.text())
    .then((result) => console.log(result))
    .catch((error) => console.log("error", error));
}

function testPOST() {
  const formdata = new FormData();
  formdata.append("c", "fbfgbdf");
  formdata.append("d", "nnfgbfbrt");

  const requestOptions = {
    method: "POST",
    body: formdata,
    redirect: "follow",
  };

  fetch("http://127.0.0.1:80/test-post?a=123123&b=hgfhfd", requestOptions)
    .then((response) => response.text())
    .then((result) => console.log(result))
    .catch((error) => console.log("error", error));
}

function setSession() {
  const formdata = new FormData();
  formdata.append("a", "1");
  formdata.append("b", "2");

  const requestOptions = {
    method: "POST",
    body: formdata,
    redirect: "follow",
  };

  fetch("http://127.0.0.1:80/set-session", requestOptions)
    .then((response) => response.text())
    .then((result) => console.log(result))
    .catch((error) => console.log("error", error));
}

function getSession() {
  const requestOptions = {
    method: "GET",
    redirect: "follow",
  };

  fetch("http://127.0.0.1:80/get-session", requestOptions)
    .then((response) => response.text())
    .then((result) => console.log(result))
    .catch((error) => console.log("error", error));
}

function testJson() {
    const myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json");
    myHeaders.append("Accept", "*/*");
    myHeaders.append("Host", "127.0.0.1:80");
    myHeaders.append("Connection", "keep-alive");

    const raw = JSON.stringify({
       "name": "userName",
       "age": "20",
       "address": "南阳"
    });

    const requestOptions = {
       method: 'POST',
       headers: myHeaders,
       body: raw,
       redirect: 'follow'
    };

    fetch("http://127.0.0.1:80/test-json", requestOptions)
       .then(response => response.text())
       .then(result => console.log(result))
       .catch(error => console.log('error', error));
}