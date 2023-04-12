<?php

/**
 * Created by Reliese Model.
 */

namespace App\Models\Base;

use App\Models\Role;
use Illuminate\Database\Eloquent\Model;

/**
 * Class RoleUser
 * 
 * @property int $role_id
 * @property int $user_id
 * @property string $user_type
 * 
 * @property Role $role
 *
 * @package App\Models\Base
 */
class RoleUser extends Model
{
	protected $table = 'role_user';
	public $incrementing = false;
	public $timestamps = false;

	protected $casts = [
		'role_id' => 'int',
		'user_id' => 'int'
	];

	public function role()
	{
		return $this->belongsTo(Role::class);
	}
}
