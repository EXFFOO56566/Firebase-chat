<?php

/**
 * Created by Reliese Model.
 */

namespace App\Models\Base;

use App\Models\Permission;
use App\Models\RoleUser;
use Carbon\Carbon;
use Illuminate\Database\Eloquent\Collection;
use Illuminate\Database\Eloquent\Model;

/**
 * Class Role
 * 
 * @property int $id
 * @property string $name
 * @property string $display_name
 * @property string $description
 * @property Carbon $created_at
 * @property Carbon $updated_at
 * 
 * @property Collection|Permission[] $permissions
 * @property Collection|RoleUser[] $role_users
 *
 * @package App\Models\Base
 */
class Role extends Model
{
	protected $table = 'roles';

	public function permissions()
	{
		return $this->belongsToMany(Permission::class);
	}

	public function role_users()
	{
		return $this->hasMany(RoleUser::class);
	}
}
