const formData = new FormData();
formData.append("foo", "123");
formData.append("bar", "456");

const xhr = new XMLHttpRequest();
xhr.open("POST", "http://localhost:8080");
xhr.setRequestHeader("Content-Type", "multipart/form-data");
xhr.send(formData);