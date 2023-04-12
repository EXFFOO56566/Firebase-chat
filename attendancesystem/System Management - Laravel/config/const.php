<?php

return [

    /*
    |--------------------------------------------------------------------------
    | write constants for this project
    |--------------------------------------------------------------------------
    */

    'SUCCESS_CREATE_MESSAGE' => 'The entered content was saved.',        // success when data created
    'FAILED_CREATE_MESSAGE'  => 'We could not save your entries.', // failed when data created
    'SUCCESS_UPDATE_MESSAGE' => 'The edited content was saved.',        // success when data updated
    'FAILED_UPDATE_MESSAGE'  => 'The edited content could not be saved.', // failed when data updated
    'SUCCESS_DELETE_MESSAGE' => 'The target data was deleted.',        // success when data deleted
    'FAILED_DELETE_MESSAGE'  => 'The target data could not be deleted.', // failed when data deleted
    'FAILED_DELETE_SELF_MESSAGE'  => 'The target data could not be deleted, can\'t delete your account.', // failed when data deleted self account
    'FAILED_VALIDATOR'       => 'Please check the form below for errors.', // failed when validator not pass
    'ERROR_FOREIGN_KEY' => 'Cannot delete this data, because this data is used in other data.',

    'UPLOAD_PATH' => public_path('uploads/'),

];
