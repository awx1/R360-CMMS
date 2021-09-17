const express = require('express');
const bodyParser = require('body-parser');
const app = express();
const port = 3000;


app.use(bodyParser.json());

app.use(bodyParser.urlencoded({
    extended: true
}));

app.get('/api/helloworld', (req, res) => {
    res.send('Hello World!')
});

app.post('/api/postTest', (req, res) => {
    console.log(req.body);
    res.send({data: 'You sent me: ' + req.body.message});
});

app.listen(port, () => {
    console.log(`CMMS Web API listening at http://localhost:${port}`)
});
