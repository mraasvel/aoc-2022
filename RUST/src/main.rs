use std::{
    collections::HashMap,
    io::BufRead,
};

const TIME_LIMIT: i32 = 30;

#[derive(Debug, Clone)]
struct Worker {
    minute: i32,
    position: usize,
    score: i32,
}

impl Worker {
    fn new(position: usize) -> Worker {
        Worker {
            minute: 0,
            position,
            score: 0,
        }
    }

    // cost should include cost to open valve aka distance + 1
    fn move_to(&self, cost: i32, valve: usize, flow_rate: i32) -> Worker {
        assert!(cost + self.minute < TIME_LIMIT);
        let minute = self.minute + cost;
        let score = self.score + flow_rate * (TIME_LIMIT - minute);
        Worker {
            minute,
            position: valve,
            score,
        }
    }

    fn can_do_move(&self, cost: i32) -> bool {
        self.minute + cost < TIME_LIMIT
    }
}

#[derive(Debug)]
struct Valve {
    flow_rate: i32,
    distances: Vec<usize>,
}

impl Valve {
    fn new(flow_rate: i32) -> Valve {
        Valve {
            flow_rate,
            distances: vec![],
        }
    }

    fn set_distances(&mut self, distances: Vec<usize>) {
        self.distances = distances;
    }

    fn distance(&self, to: usize) -> usize {
        self.distances[to]
    }
}

#[derive(Clone, Debug)]
struct State {
me: Worker,
    opened: Vec<bool>,
}

impl State {
    fn new(me: Worker, opened: Vec<bool>) -> State {
        State { me, opened }
    }

    fn get_worker(&self) -> &Worker {
        &self.me
    }
}

#[derive(Debug)]
struct Solver {
    valves: Vec<Valve>,
}

impl Solver {
    fn new(valves: Vec<Valve>) -> Solver {
        Solver { valves }
    }

    fn initial_state(&self, start: usize) -> State {
        State::new(
            Worker::new(start),
            self.valves
                .iter()
                .map(|valve| valve.flow_rate == 0)
                .collect(),
        )
    }

    fn part_one(&mut self, start: usize) -> i32 {
        let state = self.initial_state(start);
        let mut states = vec![state];
        let mut state: Option<State> = None;
        while !states.is_empty() {
            let next = states.pop().unwrap();

            // cannot put this in a seperate function because the borrow checker doesn't understand the valves reference
            let iter = self
                .valves
                .iter()
                .enumerate()
                .filter(|&(index, _)| !next.opened[index])
                .filter_map(|(index, valve)| {
                    let worker = next.get_worker();
                    let cost = 1 + valve.distance(worker.position) as i32;
                    if !worker.can_do_move(cost) {
                        None
                    } else {
                        let me = worker.move_to(cost, index, valve.flow_rate);
                        Some(State {
                            // assign to different worker in p2
                            me,
                            // copy opened vector: set new open value to true
                            opened: next
                                .opened
                                .iter()
                                .enumerate()
                                .map(|(i, &open)| open || i == index)
                                .collect(),
                        })
                    }
                });

            states.extend(iter);
            if state.is_none() || next.me.score > state.as_ref().unwrap().me.score {
                state = Some(next);
            }
        }
        dbg!(&state);
        state.unwrap().me.score
    }
}

/// A -> B = 1
/// B -> C = 1
/// C -> D = 1
/// A -> C = 2
/// A -> D = 3
/// B -> D = 2
fn example() {
    let start = 0;
    let mut valves = vec![Valve::new(0), Valve::new(13), Valve::new(9), Valve::new(7)];
    valves[0].set_distances(vec![0, 1, 2, 3]);
    valves[1].set_distances(vec![1, 0, 1, 2]);
    valves[2].set_distances(vec![2, 1, 0, 1]);
    valves[3].set_distances(vec![3, 2, 1, 0]);
    let mut solver = Solver::new(valves);
    let p1 = solver.part_one(start);
    println!("{}", p1);
}

fn parse(filename: &str) -> (usize, Vec<Valve>) {
    #[derive(Debug)]
    struct Node {
        name: String,
        connections: Vec<String>,
        flow_rate: i32,
        distances: HashMap<String, i32>,
    }
    let lines = std::io::BufReader::new(std::fs::File::open(filename).unwrap()).lines();
    // Valve BB has flow rate=13; tunnels lead to valves CC, AA
    let mut graph = HashMap::new();
    for line in lines {
        let line = line.unwrap();
        let parts: Vec<_> = line.split_ascii_whitespace().collect();
        let name = parts[1].to_string();
        let rate = parts[4];
        let connections = &parts[9..];
        let connections = connections
            .iter()
            .map(|conn| conn.trim_end_matches(',').to_string())
            .collect();
        let flow_rate = rate[rate.find('=').unwrap() + 1..]
            .trim_end_matches(';')
            .parse::<i32>()
            .unwrap();
        graph.insert(
            name.clone(),
            Node {
                name,
                connections,
                flow_rate,
                distances: HashMap::new(),
            },
        );
    }

    // naive distance computation, not a bottleneck
    fn do_compute_distances(
        current: &str,
        distances: &mut HashMap<String, i32>,
        graph: &HashMap<String, Node>,
        distance: i32,
    ) {
        if distances.contains_key(current) && distances.get(current).unwrap() < &distance {
            return;
        }
        distances.insert(current.to_string(), distance);
        for connection in &graph.get(current).unwrap().connections {
            do_compute_distances(connection, distances, graph, distance + 1);
        }
    }

    let keys: Vec<_> = graph.keys().cloned().collect();
    for id in keys {
        let id = id.as_str();
        let mut distances = HashMap::new();
        do_compute_distances(id, &mut distances, &graph, 0);
        graph.get_mut(id).unwrap().distances = distances;
    }
    println!("finished parsing");
    let mut names = Vec::new();
    let mut valves = Vec::new();
    for (key, node) in graph.iter() {
        if node.flow_rate > 0 || node.name == "AA" {
            names.push(key.clone());
            valves.push(Valve::new(node.flow_rate));
        }
    }
    for i in 0 .. names.len() {
        for name in &names {
            let distance = graph[&names[i]].distances[name];
            valves[i].distances.push(distance as usize);
        }
    }
    let start = names.iter().position(|name| name == "AA").unwrap();
    (start, valves)
}

fn main() {
    let filename = "ex.txt";
    let (start, valves) = parse(filename);
    let mut solver = Solver::new(valves);
    let p1 = solver.part_one(start);
    println!("{}", p1);
}
