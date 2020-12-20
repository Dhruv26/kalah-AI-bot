from collections import namedtuple
import os
import subprocess


LOG_FILE_NAME = 'kalah.log'
OUTPUT_DIR = 'log_analysis'

SimulationInfo = namedtuple('SimulationInfo', ['reward', 'num_visits'])


def get_simulation_info(logs):
    sim_info = list()
    for line in logs.split('\n'):
        if line.startswith('INFO: ---'):
            reward_str, num_visits_str = line.replace('INFO: ---', '').split(',', 2)
            _, reward = reward_str.split(': ', 2)
            _, num_visits = num_visits_str.split(': ', 2)
            sim_info.append(SimulationInfo(float(reward), int(num_visits)))

    return sim_info


def play_game():
    return subprocess.run(
        ['java', '-jar', 'ManKalah.jar', '"java', '-jar', 'Test_Agents/MKRefAgent.jar"', '"java', '-cp',
         'jar/Agent51.jar Main"']
    )


def delete_log_file():
    os.remove(LOG_FILE_NAME)


#play_game()
with open(LOG_FILE_NAME, 'r') as f:
    logs = f.read()
sims_lines = get_simulation_info(logs)
with open(os.path.join(OUTPUT_DIR, 'simulation_10.txt'), 'w+') as f:
    #import pdb; pdb.set_trace()
    for line in sims_lines:
        f.write(f'Reward={line.reward},Visits={line.num_visits}\n')
#delete_log_file()
