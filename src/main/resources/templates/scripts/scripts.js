 function toggleStep(id) {
        if (document.getElementById(id).classList.contains('show')) {
            document.getElementById(id).classList.remove('show')
            document.getElementById(id).classList.add('hide')
        } else {
            document.getElementById(id).classList.remove('hide')
            document.getElementById(id).classList.add('show')
        }
    }