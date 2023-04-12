"use strict";
$(function () {
    productVerify();

    $(document).on('click', '#purchase', function (){
        Swal.fire({
            title: 'Purchase Code',
            input: 'text',
            inputPlaceholder: '3756623c-5971-17de-7c2d-1cbec0d86a5e',
            html: 'Please fill the purchase code.<br>How to find the purchase code? <a href="https://help.market.envato.com/hc/en-us/articles/202822600-Where-Is-My-Purchase-Code-" target="_blank">here</a>',
            inputAttributes: {
                autocapitalize: 'off'
            },
            showCancelButton: true,
            confirmButtonText: 'Save',
            cancelButtonText: 'Close',
            onOpen: () => Swal.getConfirmButton().focus()
        }).then((result) => {
            if (result.value) {
                $('.reload').removeClass('hide');

                $.ajax({
                    url: baseUrl + '/api/helper/'+ $('.swal2-input').val(),
                    type: 'get',
                    success: function (data) {
                        $('.reload').addClass('hide');

                        if (data == 'Success') {
                            $('.ml-2.mb-1.close').trigger('click');

                            Swal.fire(
                                'Successful!',
                                'Thank you for buying our product.',
                                'success'
                            );
                        } else {
                            Swal.fire(
                                'Error! ',
                                data,
                                'error'
                            );
                        }
                    }
                });
            }
        })
    });

    $(document).on('click', '#reinput', function (){
        Swal.fire({
            title: 'Reinput Purchase Code',
            input: 'text',
            inputPlaceholder: '3756623c-5971-17de-7c2d-1cbec0d86a5e',
            html: 'Please fill the purchase code.<br>How to find the purchase code? <a href="https://help.market.envato.com/hc/en-us/articles/202822600-Where-Is-My-Purchase-Code-" target="_blank">here</a>',
            inputAttributes: {
                autocapitalize: 'off'
            },
            showCancelButton: true,
            confirmButtonText: 'Save',
            cancelButtonText: 'Close',
            onOpen: () => Swal.getConfirmButton().focus()
        }).then((result) => {
            if (result.value) {
                $('.reload').removeClass('hide');
                var csrf = document.querySelector('meta[name="csrf-token"]').content;
                var postFormData = {
                    '_token' : csrf,
                }
                $.ajax({
                    url: baseUrl + '/reinputkey/index/'+ $('.swal2-input').val(),
                    type: 'post',
                    data: postFormData,
                    success: function (data) {
                        $('.reload').addClass('hide');

                        console.log(data);

                        if (data == 'Success') {
                            $('.ml-2.mb-1.close').trigger('click');

                            Swal.fire(
                                'Successful!',
                                'Thank you.',
                                'success'
                            );
                        } else {
                            Swal.fire(
                                'Error! ',
                                data,
                                'error'
                            );
                        }
                    }
                });
            }
        })
    });
});

function productVerify() {
    $.ajax({
        url: baseUrl + '/checkProductVerify',
        type: 'get',
        success: function (dataResponse) {
            if(dataResponse == 0){
                $(document).Toasts('create', {
                    class: "bg-danger",
                    title: 'Fill the purchase code!',
                    subtitle: GetTodayDate(),
                    body: "Please fill the purchase code. Click <span id='purchase'><u>here to fill the purchase code</u></span>. You have <b>7</b> days to verify the purchase code.",
                    icon: 'fas fa-exclamation-triangle fa-lg',
                });
            }

            if(dataResponse == 5){
                $(document).Toasts('create', {
                    class: "bg-warning",
                    title: 'Refill your purchase code!',
                    subtitle: GetTodayDate(),
                    body: "Please refill your purchase code. Click <span id='purchase'><u>here to fill the purchase code</u></span>. You have <b>7</b> days to verify the purchase code.",
                    icon: 'fas fa-exclamation-triangle fa-lg',
                });
            }

            if(dataResponse == 9){
                $(document).Toasts('create', {
                    class: "bg-warning",
                    title: 'Duplicate Purchase Code!',
                    subtitle: GetTodayDate(),
                    body: "The purchase code that you entered is already owned. If you feel the purchase code belongs to you, and you want to change it, please fill your purchase code. Click <span id='reinput'><u>here to fill the purchase code</u></span>. You have <b>7</b> days to verify the purchase code.",
                    icon: 'fas fa-exclamation-triangle fa-lg',
                });
            }
        }
    });
}

function GetTodayDate() {
    var tdate = new Date();
    var dd = tdate.getDate(); //yields day
    var MM = tdate.getMonth(); //yields month
    var yyyy = tdate.getFullYear(); //yields year
    var currentDate= dd + "-" +( MM+1) + "-" + yyyy;
    return currentDate;
}
